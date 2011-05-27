(ns info.papyri.indexer
  (:gen-class
   :name info.papyri.indexer
   :methods [#^{:static true} [index [java.util.List] void]
             #^{:static true} [loadLemmas [] void]])
  (:use clojure.contrib.math)
  (:import
    (clojure.lang ISeq)
    (com.hp.hpl.jena.rdf.model Model ModelFactory Resource ResourceFactory)
    (java.io File FileInputStream FileOutputStream FileReader StringWriter FileWriter)
    (java.net URI URL URLEncoder URLDecoder)
    (java.nio.charset Charset)
    (java.text Normalizer Normalizer$Form)
    (java.util ArrayList TreeMap)
    (java.util.concurrent Executors ConcurrentLinkedQueue ConcurrentSkipListSet)
    (javax.xml.parsers SAXParserFactory)
    (javax.xml.transform Result )
    (javax.xml.transform.sax SAXResult)
    (javax.xml.transform.stream StreamSource StreamResult)
    (net.sf.saxon Configuration PreparedStylesheet TransformerFactoryImpl)
    (net.sf.saxon.lib FeatureKeys StandardErrorListener StandardURIResolver)
    (net.sf.saxon.trans CompilerInfo XPathException)
    (org.apache.solr.client.solrj SolrServer SolrQuery)
    (org.apache.solr.client.solrj.impl CommonsHttpSolrServer StreamingUpdateSolrServer BinaryRequestWriter)
    (org.apache.solr.client.solrj.request RequestWriter)
    (org.apache.solr.common SolrInputDocument)
    (org.mulgara.connection Connection ConnectionFactory)
    (org.mulgara.query Answer ConstructQuery Query)
    (org.mulgara.query.operation Command CreateGraph)
    (org.mulgara.sparql SparqlInterpreter)
    (org.xml.sax InputSource)
    (org.xml.sax.helpers DefaultHandler)))
      
(def filepath "/data/papyri.info/idp.data")
(def xsltpath "/data/papyri.info/git/navigator/pn-xslt")
(def htpath "/data/papyri.info/pn/idp.html")
(def solrurl "http://localhost:8082/solr/")
(def numbersurl "http://localhost:8090/sparql?query=")
(def server (URI/create "rmi://localhost/server1"))
(def graph (URI/create "rmi://localhost/papyri.info#pi"))
(def nthreads 10)
(def nserver "localhost")
(def collections (ref (ConcurrentLinkedQueue.)))
(def htmltemplates (ref nil))
(def html (ref (ConcurrentLinkedQueue.)))
(def solrtemplates (ref nil))
(def text (ref (ConcurrentLinkedQueue.)))
(def texttemplates (ref (ConcurrentLinkedQueue.)))
(def links (ref (ConcurrentLinkedQueue.)))
(def documents (ref (ConcurrentLinkedQueue.)))
(def words (ref (ConcurrentSkipListSet.)))
(def *current*)
(def *doc*)
(def *index*)

(defn copy
  "Performs a file copy from the source to the destination, making directories if necessary."
  [in out]
  (try
    (let [outfile (File. out)]
      (.mkdirs (.getParentFile outfile))
      (.createNewFile outfile))
    (let [buffer (byte-array 1024)
    from (FileInputStream. in)
    to (FileOutputStream. out)]
      (loop []
  (let [size (.read from buffer)]
    (when (pos? size)
      (.write to buffer 0 size)
      (recur))))
      (.close from)
      (.close to))
    (catch Exception e
      (println (str (.getMessage e) " copying " in " to " out ".")))))

(defn init-templates
  "Initialize XSLT template pool."
    [xslt, nthreads, pool]
  (dosync (ref-set (load-string pool) (ConcurrentLinkedQueue.) ))
  (dotimes [n nthreads]
    (let [xsl-src (StreamSource. (FileInputStream. xslt))
            configuration (Configuration.)
            compiler-info (CompilerInfo.)]
          (doto xsl-src 
            (.setSystemId xslt))
    (doto configuration
      (.setXIncludeAware true))
          (doto compiler-info
            (.setErrorListener (StandardErrorListener.))
            (.setURIResolver (StandardURIResolver. configuration)))
          (dosync (.add (load-string (str "@" pool)) (PreparedStylesheet/compile xsl-src configuration compiler-info))))))
            
(defn substring-after
  [string1 string2]
  (.substring string1 (if (.contains string1 string2) (+ (.indexOf string1 string2) (.length string2)) (.length string2))))

(defn substring-before
  [string1 string2]
  (.substring string1 0 (if (.contains string1 string2) (.indexOf string1 string2) 0)))

(defn encode-url
  [url]
  (URLEncoder/encode url "UTF-8"))

(defn decode-url
  [url]
  (URLDecoder/decode url "UTF-8"))

(defn get-filename
  [url]
  (if (.contains url "ddbdp/")
    (let [identifier (.split (substring-before (substring-after url "http://papyri.info/ddbdp/") "/source") ";")]
      (if (= (second identifier) "")
        (str filepath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "."
       (.replace (.replace (.replace (.replace (last identifier) "," "-") "/" "_") "%2F" "_") "%2C" "-") ".xml")
        (str filepath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier) 
       "/" (first identifier) "." (second identifier) "."
       (.replace (.replace (.replace (.replace (last identifier) "," "-") "/" "_") "%2F" "_") "%2C" "-") ".xml")))
    (if (.contains url "hgv/")
      (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
            id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
        (str filepath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".xml"))
      (when (.contains url "apis/")
        (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
          (str filepath "/APIS/" (first identifier) "/xml/" (first identifier) "." (second identifier) "." (last identifier) ".xml"))))))

(defn get-txt-filename
  [url]
  (try (if (.startsWith url "file:")
    (.replace (str htpath (substring-before (substring-after url (str "file:" filepath)) ".xml") ".txt") "/xml/" "/")
    (if (.contains url "ddbdp")
      (let [url (decode-url url)]
  (when (.endsWith url "/source")
    (let [identifier (.split (substring-before (substring-after url "http://papyri.info/ddbdp/") "/source") ";")]
      (if (= (second identifier) "")
        (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "."
       (.replace (.replace (last identifier) "," "-") "/" "_") ".txt")
        (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier) 
       "/" (first identifier) "." (second identifier) "."
       (.replace (.replace (last identifier) "," "-") "/" "_") ".txt")))))
      (if (.contains url "hgv")
        (when (.endsWith url "/source")
          (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
                id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
            (str htpath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".txt")))
        (when (.contains url "/apis")
          (if (.endsWith url "/source")
            (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
              (str htpath "/APIS/" (first identifier) "/" (first identifier) "." (second identifier) "." (last identifier) ".txt")))))))
       (catch Exception e
   (println (str (.getMessage e) " processing " url ".")))))
          
(defn get-html-filename
  [url]
  (try (if (.startsWith url "file:")
    (.replace (str htpath (substring-before (substring-after url (str "file:" filepath)) ".xml") ".html") "/xml/" "/")
    (if (.contains url "ddbdp")
      (let [url (decode-url url)]
  (if (.endsWith url "/source")
        (let [identifier (.split (substring-before (substring-after url "http://papyri.info/ddbdp/") "/source") ";")]
          (if (= (second identifier) "")
            (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "."
     (.replace (.replace (last identifier) "," "-") "/" "_") ".html")
            (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier) 
     "/" (first identifier) "." (second identifier) "."
     (.replace (.replace (last identifier) "," "-") "/" "_") ".html")))
        (if (= url "http://papyri.info/ddbdp")
          (str htpath "/DDB_EpiDoc_XML/index.html")
    (if (.contains url ";")
      (let [identifier (.split (substring-after url "http://papyri.info/ddbdp/") ";")]
        (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier) "/index.html"))
      (str htpath "/DDB_EpiDoc_XML/" (substring-after url "http://papyri.info/ddbdp/") "/index.html")))))
      (if (.contains url "hgv")
        (if (.endsWith url "/source")
          (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
                id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
            (str htpath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".html"))
          (if (= url "http://papyri.info/hgv")
            (str htpath "/HGV_meta_EpiDoc/index.html")
            (str htpath "/HGV_meta_EpiDoc/" (substring-after url "http://papyri.info/hgv/") "/index.html")))
        (when (.contains url "/apis")
          (if (.endsWith url "/source")
            (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
              (str htpath "/APIS/" (first identifier) "/" (first identifier) "." (second identifier) "." (last identifier) ".html"))
            (if (= url "http://papyri.info/apis")
              (str htpath "/APIS/index.html")
              (str htpath "/APIS/" (substring-after url "http://papyri.info/apis/") "/index.html")))))))
       (catch Exception e
   (println (str (.getMessage e) " processing " url ".")))))

(defn transform
  "Takes an java.io.InputStream, a list of key/value parameter pairs, and a javax.xml.transform.Result"
  [url, params, #^Result out, pool]
  
    (let [xslt (.poll pool)
    transformer (.newTransformer xslt)]
      (try
  (when (not (== 0 (count params)))
    (doseq [param params] (doto transformer
          (.setParameter (first param) (second param)))))
  (.transform transformer (StreamSource. (.openStream (URL. url))) out)
  (catch Exception e
    (println (str (.getMessage e) " transforming " url "."))
    (.printStackTrace e))
  (finally
   (.add pool xslt)))))
    
    
(defn has-part-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {<%s> dc:relation ?a}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a }" url url))
            
(defn batch-relation-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {?a dc:relation ?b}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a .
                    ?a dc:relation ?b}" url))

(defn relation-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            select ?a
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:relation ?a }" url))

(defn batch-replaces-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {?a dc:replaces ?b}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a .
                    ?a dc:replaces ?b }" url))

(defn replaces-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            select ?a
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:replaces ?a }" url))

(defn batch-is-replaced-by-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {?a dc:isReplacedBy ?b}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a .
                    ?a dc:isReplacedBy ?b }" url))

(defn is-replaced-by-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            select ?a
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:isReplacedBy ?a }" url))
                    
(defn execute-query
  [query]
  (let [interpreter (SparqlInterpreter.)
        answerlist (ArrayList.)
        conn (.newConnection (ConnectionFactory.) server)
        answer (.execute (.parseQuery interpreter query) conn)]
    (dotimes [_ (.getRowCount answer)]
      (when (.next answer)
        (doto answerlist (.add (let [ans (ArrayList.)]
          (dotimes [n (.getNumberOfVariables answer)]
            (.add ans (.toString (.getObject answer n))))
          (seq ans))))))
    (doto conn (.close))
    (seq answerlist)))

(defn queue-item
  [url]
  (let [relations (execute-query (relation-query url))
       replaces (execute-query (replaces-query url))
       is-replaced-by (execute-query (is-replaced-by-query url))]

    (.add @html (list (str "file:" (get-filename url))
          (list "collection" (substring-before (substring-after url "http://papyri.info/") "/"))
          (list "related" (apply str (interpose " " (for [x relations] (first x)))))
          (list "replaces" (apply str (interpose " " (for [x replaces] (first x))))) 
          (list "isReplacedBy" (apply str (interpose " " (for [x is-replaced-by] (first x)))))
          (list "server" nserver)))))
  
       

(defn queue-items
  [url exclude]
  (let [items (execute-query (has-part-query url))
        relations (execute-query (batch-relation-query url))
        replaces (execute-query (batch-replaces-query url))
        is-replaced-by (execute-query (batch-is-replaced-by-query url))]
        (doseq [item items]
             (let  [related (if (empty? relations) ()
          (filter (fn [x] (= (first x) (last item))) relations))
                    reprint-from (if (empty? replaces) ()
         (filter (fn [x] (= (first x) (last item))) replaces))
                    reprint-in (if (empty? is-replaced-by) ()
             (filter (fn [x] (= (first x) (last item))) is-replaced-by))
                    exclusion (some (set (for [x (filter 
                      (fn [s] (and (.startsWith (last s) "http://papyri.info") (not (.contains (.toString (last s)) "/images/")))) related)] 
               (substring-before (substring-after (last x) "http://papyri.info/") "/"))) exclude)]
      (if (nil? exclusion)
        (.add @html (list (str "file:" (get-filename (last item)))
          (list "collection" (substring-before (substring-after (last item) "http://papyri.info/") "/"))
          (list "related" (apply str (interpose " " (for [x related] (last x)))))
          (list "replaces" (apply str (interpose " " (for [x reprint-from] (last x))))) 
          (list "isReplacedBy" (apply str (interpose " " (for [x reprint-in] (last x)))))
          (list "server" nserver)))
        (do (.add @links (list (get-html-filename (.toString (last (reduce (fn [x y] (if (.contains (last x) exclusion) x y)) related))))
             (get-html-filename (.toString (last item)))))
      (.add @links (list (get-txt-filename (.toString (last (reduce (fn [x y] (if (.contains (last x) exclusion) x y)) related))))
             (get-txt-filename (last item))))))))))
                  
(defn queue-collections
  "Adds URLs to the HTML transform and indexing queues for processing.  Takes a URL, like http://papryi.info/ddbdp/rdf,
  a set of collections to exclude and recurses down to the item level."
  [url exclude]
  ;; TODO: generate symlinks for relations
  ;; queue for HTML generation
  (.add @html (list url (list "collection" (if (.contains (substring-after url "http://papyri.info/") "/")
        (substring-before (substring-after url "http://papyri.info/") "/")
        (substring-after url "http://papyri.info/")))
        (list "related" "") 
        (list "replaces" "") 
        (list "isReplacedBy" "")
        (list "server" nserver)))
  (let [items (execute-query (has-part-query url))]
    (when (> (count items) 0)
      (if (.endsWith (last (first items)) "/source")
        (queue-items url exclude)
        (doseq [item items]
          (queue-collections (last item) exclude))))))

(defn generate-html
  []
    (let [pool (Executors/newFixedThreadPool nthreads)
    tasks (map (fn [x]
         (fn []
           (try (.mkdirs (.getParentFile (File. (get-html-filename (first x)))))
          ;(println "Transforming " (first x) " to " (get-html-filename (first x)))
          (transform (if (.startsWith (first x) "http")
            (str (.replace (first x) "papyri.info" nserver) "/rdf")
            (first x))
          (list (second x) (nth x 2) (nth x 3) (nth x 4) (nth x 5))
          (StreamResult. (File. (get-html-filename (first x)))) @htmltemplates)
           (catch Exception e
       (.printStackTrace e)
       (println (str "Error converting file " (first x) " to " (get-html-filename (first x))))))))
       @html)]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))
    (dosync (ref-set text @html)
      (ref-set htmltemplates nil)))

(defn generate-text
  []
    (let [pool (Executors/newFixedThreadPool nthreads)
        tasks (map (fn [x]
         (fn []
           (when (not (.startsWith (first x) "http"))
       (try (.mkdirs (.getParentFile (File. (get-html-filename (first x)))))
            (transform (if (.startsWith (first x) "http")
                                           (str (.replace (first x) "papyri.info" nserver) "/rdf")
                                           (first x))
           (list (second x) (nth x 2) (nth x 3) (nth x 4))
           (StreamResult. (File. (get-txt-filename (first x)))) @texttemplates)
            (catch Exception e
        (.printStackTrace e)
        (println (str "Error converting file " (first x) " to " (get-txt-filename (first x)))))))))
       @text)]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))
    (dosync (ref-set texttemplates nil)))

(defn index-solr
  [solr]
  (.start (Thread. 
     (fn []
       (let [solr (StreamingUpdateSolrServer. solr 5000 5)]
         (.setRequestWriter solr (BinaryRequestWriter.))
         (while (= (count @documents) 0)
     (Thread/sleep 30000))
         (when (> (count @documents) 0)
     (let [docs (ArrayList.)]
       (.addAll docs @documents)
       (.removeAll @documents docs)
       (.add solr docs))))
         (Thread/sleep 30000)
         (when (> (count @documents) 0)
     (index-solr solr))))))

(defn add-words [words]
  (let [word-arr (.split words "\\s+")]
    (for [word word-arr]
      (.add @words word))))

(defn print-words []
     (let [out (FileWriter. (File. "/data/papyri.info/words.txt"))]
       (for [word @words]
   (.write out (str word "\n")))))

(defn load-morphs 
 [file]
 (let [value (StringBuilder.)
  handler (proxy [DefaultHandler] []
         (startElement [uri local qname atts]
      (set! *current* qname)
      (when (= qname "analysis")
        (set! *doc* (SolrInputDocument.))
        (set! *index* (+ *index* 1))
        (.addField *doc* "id" *index*)))
         (characters [ch start length]
          (when-not (.startsWith *current* "analys")
      (.append value ch start length)))
    (endElement [uri local qname]
          (if (not (.startsWith qname "analys"))
      (.addField *doc* *current* (.toString value))
      (do
        (dosync (.add @documents *doc*))
        (when (> (count @documents) 5000)
          (index-solr (str solrurl "morph-search/")))))
          (set! *current* "analysis")
          (.delete value 0 (.length value)))
    (endDocument []
           (index-solr (str solrurl "morph-search/"))))]
   (.. SAXParserFactory newInstance newSAXParser
                   (parse (InputSource. (FileInputStream. file)) 
         handler))))
         
(defn -loadLemmas []
  (binding [*current* nil
      *doc* nil
      *index* 0]
    (index-solr (str solrurl "morph-search/"))
    (load-morphs "/data/papyri.info/git/navigator/pn-lemmas/greek.morph.unicode.xml")
    (load-morphs "/data/papyri.info/git/navigator/pn-lemmas/latin.morph.xml")
    (let [solr (CommonsHttpSolrServer. (str solrurl "morph-search/"))]
      (.commit solr))))
   
(defn -index [& args]
  (println args)
  (init-templates (str xsltpath "/RDF2HTML.xsl") nthreads "info.papyri.indexer/htmltemplates")
  (init-templates (str xsltpath "/RDF2Solr.xsl") nthreads "info.papyri.indexer/solrtemplates")
  (init-templates (str xsltpath "/MakeText.xsl") nthreads "info.papyri.indexer/texttemplates")

  (if (nil? (first args))
    (do
      (println "Queueing DDbDP...")
      (queue-collections "http://papyri.info/ddbdp" ())
      (println (str "Queued " (count @html) " documents."))
      (println "Queueing HGV...")
      (queue-collections "http://papyri.info/hgv" '("ddbdp"))
      (println (str "Queued " (count @html) " documents."))
      (println "Queueing APIS...")
      (queue-collections "http://papyri.info/apis" '("ddbdp", "hgv"))
      (println (str "Queued " (count @html) " documents.")))
    (doseq [arg args] (queue-item arg)))

  (dosync (ref-set text @html))
  
  (println (str "Queued " (count @html) " documents."))
 
  ;; Generate HTML
  (println "Generating HTML...")
  (generate-html)

  ;; Generate text
  (println "Generating text...")
  (generate-text)
  
  ;; Start Solr indexing thread if we're doing the lot
  (when (nil? (first args))
    (index-solr (str solrurl "pn-search-offline/")))
  
  ;; Index docs queued in @text
  (println "Indexing text...")
   (let [pool (Executors/newFixedThreadPool nthreads)
        tasks
  (map (fn [x]
         (fn []
     (when (not (.startsWith (first x) "http"))
           (transform (first x)
                (list (second x) (nth x 2))
                (SAXResult. 
                 (let [current (StringBuilder.)
                 chars  (StringBuilder.)
                 solrdoc (SolrInputDocument.)]
                   (proxy [DefaultHandler] []
               (startElement [uri local qname atts]
                       (when (= local "field")
                   (doto current (.append (.getValue atts "name")))
                   (when (> (.length chars) 0)
                     (doto chars (.delete 0 (.length chars))))))
               (characters [ch start length]
                     (doto chars (.append ch start length)))
               (endElement [uri local qname]
                     (when (> (.length current) 0)
                       (.addField solrdoc (.toString current) (.toString chars))
                       (doto current (.delete 0 (.length current)))))
               (endDocument []
                      (when (not (nil? (.getField solrdoc "transcription")))
                  (add-words (.getFieldValue solrdoc "transcription")))
                      (.add @documents solrdoc))))) @solrtemplates)))) @text)]
         (doseq [future (.invokeAll pool tasks)]
           (.get future))
         (doto pool
           (.shutdown)))
  (when (> (count @documents) 0)
    (index-solr (str solrurl "pn-search-offline/")))

  (dosync (ref-set html nil)
    (ref-set text nil)
    (ref-set solrtemplates nil))
  
  (let [solr (CommonsHttpSolrServer. (str solrurl "pn-search-offline/"))]
    (doto solr 
      (.commit)
      (.optimize)))
  (print-words))
       

(defn -main [& args]
  (if (> (count args) 0)
    (if (= (first args) "load-lemmas")
      (-loadLemmas))
    (-index))
  )


(ns info.papyri.indexer
  (:use clojure.contrib.math)
  (:import 
    (com.hp.hpl.jena.rdf.model Model ModelFactory Resource ResourceFactory)
    (java.io File FileInputStream FileReader StringWriter)
    (java.net URI URL URLEncoder)
    (java.nio.charset Charset)
    (java.text Normalizer Normalizer$Form)
    (java.util ArrayList TreeMap)
    (java.util.concurrent Executors ConcurrentLinkedQueue)
    (javax.xml.parsers SAXParserFactory)
    (javax.xml.transform Result )
    (javax.xml.transform.sax SAXResult)
    (javax.xml.transform.stream StreamSource StreamResult)
    (net.sf.saxon Configuration FeatureKeys PreparedStylesheet StandardErrorListener StandardURIResolver TransformerFactoryImpl)
    (net.sf.saxon.trans CompilerInfo XPathException)
    (org.apache.solr.client.solrj SolrServer)
    (org.apache.solr.client.solrj.impl StreamingUpdateSolrServer BinaryRequestWriter)
    (org.apache.solr.client.solrj.request RequestWriter)
    (org.apache.solr.common SolrInputDocument)
    (org.mulgara.connection Connection ConnectionFactory)
    (org.mulgara.query ConstructQuery Query)
    (org.mulgara.query.operation Command CreateGraph)
    (org.mulgara.sparql SparqlInterpreter)
    (org.xml.sax InputSource)
    (org.xml.sax.helpers DefaultHandler)))
      
(def filepath "/Users/hcayless/Development/APIS/idp.data")
(def xsltpath "/Users/hcayless/Development/APIS/idp/pn/trunk/pn-xslt")
(def htpath "/Users/hcayless/Development/APIS/idp.html")
(def solrurl "http://localhost:8080/solr/")
(def numbersurl "http://papyri.info/mulgara/sparql?query=")
(def server (URI/create "rmi://localhost/server1"))
(def graph (URI/create "rmi://localhost/papyri.info#pi"))
(def conn (.newConnection (ConnectionFactory.) server))
(def collections (ref (ConcurrentLinkedQueue.)))
(def htmltemplates (ref nil))
(def html (ref (ConcurrentLinkedQueue.)))
(def texttemplates (ref nil))
(def text (ref (ConcurrentLinkedQueue.)))
(def links (ref (ConcurrentLinkedQueue.)))
(def documents (ref (ConcurrentLinkedQueue.)))
(def morphs (ref (TreeMap.)))


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
          
(defn get-html-filename
  [url]
  (if (.startsWith url "file:")
    (str htpath (substring-before (substring-after url (str "file:" filepath)) ".xml") ".html")
    (if (.contains url "ddbdp")
      (if (.endsWith url "/source")
        (let [identifier (.split (substring-before (substring-after url "http://papyri.info/ddbdp/") "/source") ";")]
          (if (= (second identifier) "")
            (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "."
		 (.replace (.replace (.replace (.replace (last identifier) "," "-") "/" "_") "%2F" "_") "%2C" "-") ".html")
            (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier) 
		 "/" (first identifier) "." (second identifier) "."
		 (.replace (.replace (.replace (.replace (last identifier) "," "-") "/" "_") "%2F" "_") "%2C" "-") ".html")))
        (if (= url "http://papyri.info/ddbdp")
          (str htpath "/DDB_EpiDoc_XML/index.html")
	  (if (.contains url ";")
	    (let [identifier (.split (substring-after url "http://papyri.info/ddbdp/") ";")]
	      (str htpath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier) "/index.html"))
	    (str htpath "/DDB_EpiDoc_XML/" (substring-after url "http://papyri.info/ddbdp/") "/index.html"))))
      (if (.contains url "hgv")
        (if (.endsWith url "/source")
          (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
                id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
            (str filepath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".html"))
          (if (= url "http://papyri.info/hgv")
            (str htpath "/HGV_meta_EpiDoc/index.html")
            (str htpath "/HGV_meta_EpiDoc/" (substring-after url "http://papyri.info/hgv/") "/index.html")))
        (when (.contains url "/apis")
          (if (.endsWith url "/source")
            (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
              (str htpath "/APIS/" (first identifier) "/" (first identifier) "." (second identifier) "." (last identifier) ".html"))
            (if (= url "http://papyri.info/apis")
              (str htpath "/APIS/index.html")
              (str htpath "/APIS/" (substring-after url "http://papyri.info/apis/") "/index.html"))))))))

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
        (println (str (.getMessage e) " processing file " url))
        (.printStackTrace e)))
    (.add pool xslt)))
    
(defn has-part-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {<%s> dc:relation ?a}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a }" url url))
            
(defn relation-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {?a dc:relation ?b}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a .
                    ?a dc:relation ?b }" url))

(defn replaces-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {?a dc:replaces ?b}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a .
                    ?a dc:replaces ?b }" url))

(defn is-replaced-by-query
  [url]
  (format  "prefix dc: <http://purl.org/dc/terms/> 
            construct {?a dc:isReplacedBy ?b}
            from <rmi://localhost/papyri.info#pi>
            where { <%s> dc:hasPart ?a .
                    ?a dc:isReplacedBy ?b }" url))
                    
(defn execute-query
  [query]
  (let [interpreter (SparqlInterpreter.)]
    (.execute (.parseQuery interpreter query) conn)))

(defn get-model
  [answer]
  (let [model (ModelFactory/createDefaultModel)]
    (while (.next answer)
      (doto model
	(.add (ResourceFactory/createStatement
	       (ResourceFactory/createResource (.toString (.getObject answer 0)))
	       (ResourceFactory/createProperty (.toString (.getObject answer 1)))
	       (ResourceFactory/createResource (.toString (.getObject answer 2)))))))
    model))

(defn answer-seq
  [answer]
  (when (.next answer)
    (cons (list (.getObject answer 0) (.getObject answer 1) (.getObject answer 2)) (answer-seq answer))))
    
            
(defn queue-items
  [url exclude]
  (let [items (execute-query (has-part-query url))
        relations (answer-seq (execute-query (relation-query url)))
        replaces (answer-seq (execute-query (replaces-query url)))
        is-replaced-by (answer-seq (execute-query (is-replaced-by-query url)))]
        (while (.next items)
          (let [item (.getObject items 2)
                related (if (empty? relations) ()
			    (filter (fn [x] (= (first x) item)) relations))
                reprint-from (if (empty? replaces) ()
				 (filter (fn [x] (= (first x) item)) replaces))
                reprint-in (if (empty? is-replaced-by) ()
			       (filter (fn [x] (= (first x) item)) is-replaced-by))
                exclusion (some (set (for [x (filter 
                  (fn [x] (.startsWith (.toString x) "http://papyri.info")) related)] 
				       (substring-before (substring-after (.toString x) "http://papyri.info/") "/"))) exclude)]
	    (if (nil? exclusion)
	      (do 
		(.add @html (list (str "file:" (get-filename (.toString item)))
				  (list "collection" (substring-before (substring-after (.toString item) "http://papyri.info/") "/"))
				  (list "related" (apply str (interpose " " (for [x related] (.toString (last x))))))
				  (list "replaces" (apply str (interpose " " (for [x reprint-from] (.toString (last x)))))) 
				  (list "isReplacedBy" (apply str (interpose " " (for [x reprint-in] (.toString (last x))))))))
		(.add @text (list (str "file:" (get-filename (.toString item)))
				  (list "collection" (substring-before (substring-after (.toString item) "http://papyri.info/") "/"))
				  (list "related" (apply str (interpose " " (for [x related] (.toString (last x)))))))))
	      (.add @links (list (get-html-filename (.toString item)) 
				 (get-html-filename (.toString (reduce (fn [x y] (if (.contains (.toString x) exclusion) x y)) related))))))))))
                  
(defn queue-collections
  "Adds URLs to the HTML transform and indexing queues for processing.  Takes a URL, like http://papryi.info/ddbdp/rdf,
  a set of collections to exclude and recurses down to the item level."
  [url exclude]
  ;; TODO: generate symlinks for relations
  ;; queue for HTML generation
  (.add @html (list url (list "collection" (if (.contains (substring-after url "http://papyri.info/") "/")
			  (substring-before (substring-after url "http://papyri.info/") "/")
			  (substring-after url "http://papyri.info/")))
		    (list "related" "") (list "replaces" "") (list "isReplacedBy" "")))
  (let [items (execute-query (has-part-query url))]
    (when (.next items)
      (if (.endsWith (.toString (.getObject items 2)) "/source")
        (queue-items url exclude)
        (do 
          (queue-collections (.toString (.getObject items 2)) exclude)
          (while (.next items)
            (queue-collections (.toString (.getObject items 2)) exclude)))))))
      
(defn get-lemmas
  [text]
  (apply str (interpose " "  
    (list* (for [word (.split text "\\s+")]
      (apply str (interpose " " (.get @morphs (Normalizer/normalize 
        (.replace (Normalizer/normalize word Normalizer$Form/NFD) "\u0300" "\u0301") Normalizer$Form/NFC)))))))))
        
(defn load-morphs 
  [file]
  (let [form (StringBuilder.)
        lemma (StringBuilder.)
        current (StringBuilder.)
        handler (proxy [DefaultHandler] []
          (startElement [uri local qname atts]
            (doto current (.delete 0 (.length current)))
            (doto current (.append qname))
            (when (= qname "lemma")
              (when (> (.length lemma) 0)
                (doto lemma (.delete 0 (.length lemma)))))
            (when (= qname "form")
              (when (> (.length form) 0)
                (doto form (.delete 0 (.length form))))))
          (characters [ch start length]
            (when (= (.toString current) "lemma")
              (doto lemma (.append ch start length)))
            (when (= (.toString current) "form")
              (doto form (.append ch start length))))
          (endElement [uri local qname]
            (when (= qname "analysis")
              (dosync (.put @morphs (.trim (.toString form)) 
                (conj (set (.get @morphs (.trim (.toString form)))) (.trim (.toString lemma))))))))]
    (.. SAXParserFactory newInstance newSAXParser
                    (parse (InputSource. (FileInputStream. file)) 
			   handler))))
(defn index-solr
  []
  (.start (Thread. 
    (fn [] 
      (let [solr (StreamingUpdateSolrServer. solrurl 500 5)]
        (doto solr (.setRequestWriter (BinaryRequestWriter.)))
        (while (= (count @documents) 0)
          (Thread/sleep 1000))
        (while (> (count @documents) 0)
          (doto solr (.add (.poll @documents))))
        (doto solr (.commit))
        (doto solr (.optimize))
	(Thread/sleep 1000)
	(when (> (count @documents) 0)
	  (index-solr)))))))



(defn -main [& args]

  (init-templates (str xsltpath "/" "RDF2HTML.xsl") 20 "htmltemplates")
  (init-templates (str xsltpath "/" "RDF2Solr.xsl") 20 "texttemplates")
  (println "Queueing DDbDP...")
  (queue-collections "http://papyri.info/ddbdp" ())
  (println (str "Queued " (count @html) " documents."))
  (println "Queueing HGV...")
  (queue-collections "http://papyri.info/hgv" '("ddbdp"))
  (println (str "Queued " (count @html) " documents."))
  (println "Queueing APIS...")
  (queue-collections "http://papyri.info/apis" '("ddbdp", "hgv"))
  (println (str "Queued " (count @html) " documents."))
  
  ;; Generate HTML
  (println "Generating HTML...")
  (let [pool (Executors/newFixedThreadPool 10)
        tasks (map (fn [x]
		     (fn []
		       (try (.mkdirs (.getParentFile (File. (get-html-filename (first x)))))
					;(println "Transforming " (first x) " to " (get-html-filename (first x)))
		       (transform (if (.startsWith (first x) "http")
				    (str (first x) "/rdf")
				    (first x))
				  (list (second x) (nth x 2) (nth x 3) (nth x 4))
				  (StreamResult. (File. (get-html-filename (first x)))) @htmltemplates)
		       (catch Exception e
			 (.printStackTrace e)
			 (println (str "Error converting file " (first x) "to" (get-html-filename (first x))))))))
		   @html)]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))
      
  ;; Generate symlinks
  (println "Generating symlinks...")
  (let [pool (Executors/newFixedThreadPool 10)
        tasks (map (fn [files]
    (fn [] 
        (.mkdirs (.getParentFile (File. (last files))))
        (.exec (Runtime/getRuntime) (str "ln -s " (first files) " " (last files)))))
    @links)]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
      (doto pool
        (.shutdown)))

  (println "Loading morphs...")
  (let [files '("/Users/hcayless/Development/APIS/idp/pn/trunk/pn-lemmas/greek.morph.unicode.xml"
		"/Users/hcayless/Development/APIS/idp/pn/trunk/pn-lemmas/latin.morph.xml")
	pool (Executors/newFixedThreadPool (count files))
          tasks (map (fn [file]
            (fn [] 
	      (load-morphs file)))
		     files)]
     (doseq [future (.invokeAll pool tasks)]
        (.get future))
        (doto pool
          (.shutdown)))
  
  ;; Start Solr indexing thread
  (index-solr)
  
  ;; Index docs queued in @text
  (println "Indexing text...")
  (let [pool (Executors/newFixedThreadPool 10)
        tasks (map (fn [x]
    (fn []
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
                    (.addField solrdoc "transcription_l" (get-lemmas (.getFieldValue solrdoc "transcription"))))
                  (.add @documents solrdoc))))) @texttemplates))) @text)]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown))))
  
(-main)
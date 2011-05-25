;; Recursively read a directory full of XML and convert the files therein to RDF, using a provided XSLT.
;; Then load the RDF data into a triplestore

(ns info.papyri.map
  (:gen-class
   :name info.papyri.map
   :methods [#^{:static true} [deleteGraph [] void]
            #^{:static true} [deleteUri [String] void]
            #^{:static true} [loadFile [String] void]
            #^{:static true} [insertInferences [String] void]
            #^{:static true} [mapFiles [java.util.List] void]
            #^{:static true} [mapAll [java.util.List] void]
            ])
  (:import (java.io BufferedReader ByteArrayInputStream ByteArrayOutputStream File FileInputStream FileOutputStream FileReader StringWriter)
           (java.net URI)
           (java.nio.charset Charset)
           (java.util.concurrent Executors ConcurrentLinkedQueue)
           (javax.activation MimeType)
           (javax.xml.transform Templates Transformer)
           (javax.xml.transform.stream StreamSource StreamResult)
           (net.sf.saxon Configuration FeatureKeys StandardErrorListener StandardURIResolver PreparedStylesheet TransformerFactoryImpl)
           ;;(net.sf.saxon.lib FeatureKeys StandardErrorListener StandardURIResolver)
           (net.sf.saxon.trans CompilerInfo XPathException)
           (org.mulgara.connection Connection ConnectionFactory)
           (org.mulgara.query Answer Query)
           (org.mulgara.query.operation Command CreateGraph Insertion Load Deletion DropGraph)
           (org.mulgara.sparql SparqlInterpreter)
           (org.mulgara.itql TqlInterpreter)))
           
(def pxslt (ref nil))
(def buffer (ref nil))
(def flushing (ref false))
(def output (ref nil))
(def server (URI/create "rmi://localhost/server1"))
(def graph (URI/create "rmi://localhost/papyri.info#pi"))
(def param (ref nil))
;; NOTE hard-coded file and directory locations
(def xslts {"DDB_EpiDoc_XML" "/data/papyri.info/git/navigator/pn-mapping/xslt/ddbdp-rdf.xsl",
      "HGV_meta_EpiDoc" "/data/papyri.info/git/navigator/pn-mapping/xslt/hgv-rdf.xsl",
      "APIS" "/data/papyri.info/git/navigator/pn-mapping/xslt/apis-rdf.xsl",
      "HGV_trans_EpiDoc" "/data/papyri.info/git/navigator/pn-mapping/xslt/hgvtrans-rdf.xsl"})
(def idproot "/data/papyri.info/idp.data")
(def ddbroot "/data/papyri.info/idp.data/DDB_EpiDoc_XML")
(def help (str "Usage: <function> [<params>]\n"
     "Functions: map-all <directory>, map-files <file list>, load-file <file>, "
     "delete-graph, delete-uri <uri>, insert-inferences <uri>."))
     
(defn substring-before
  [string1 string2]
  (.substring string1 0 (if (.contains string1 string2) (.indexOf string1 string2) 0)))

(defn flush-buffer [n]
  (let [rdf (StringBuffer.)
        times (if (not (nil? n)) n 500)
        factory (ConnectionFactory.)
        conn (.newConnection factory server)]
    (doto rdf
      (.append "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" 
      xmlns:dcterms=\"http://purl.org/dc/terms/\" 
      xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">"))
    (dotimes [n times]
      (let [string (.poll @buffer)]
        (if (not (nil? string))
          (.append rdf string))))
    (doto rdf
      (.append "</rdf:RDF>"))
    (.execute (Load. graph (ByteArrayInputStream. (.getBytes (.toString rdf) (Charset/forName "UTF-8"))) (MimeType. "application/rdf+xml")) conn)
    (doto conn
      (.close))))

(defn transform
  [file]
  (let [transformer (.newTransformer @pxslt)
        out (StringWriter.)
        outstr (StreamResult. out)]
    (try
      (if (not (nil? @param))
        (doto transformer
          (.setParameter (first @param) (second @param))))
      (.transform transformer (StreamSource. (FileInputStream. file)) outstr)
      ;; (println (.toString out))
      (.add @buffer (.toString out))
      (catch Exception e 
        (.println *err* (str (.getMessage e) " processing file " file))))))

(defn init-xslt
    [xslt]
  (dosync (ref-set buffer (ConcurrentLinkedQueue.) ))
  (let [xsl-src (StreamSource. (FileInputStream. xslt))
        configuration (Configuration.)
        compiler-info (CompilerInfo.)]
        (doto xsl-src 
          (.setSystemId xslt))
        (doto compiler-info
          (.setErrorListener (StandardErrorListener.))
          (.setURIResolver (StandardURIResolver. configuration)))
        (dosync (ref-set pxslt (PreparedStylesheet/compile xsl-src configuration compiler-info)))))
          
(defn choose-xslt
  [file]
  (cond (.contains (str file) "DDB_EpiDoc_XML") (xslts "DDB_EpiDoc_XML")
    (.contains (str file) "HGV_meta_EpiDoc") (xslts "HGV_meta_EpiDoc")
    (.contains (str file) "APIS") (xslts "APIS")
    (.contains (str file) "HGV_trans_EpiDoc") (xslts "HGV_trans_EpiDoc")))
    
(defn format-url-query
  [filename] 
  (format 
    "prefix dc: <http://purl.org/dc/terms/> 
    select ?uri
    from <rmi://localhost/papyri.info#pi>
    where {?uri dc:identifier \"%s\"}"))
    
(defn execute-query
  [query]
  (let [interpreter (SparqlInterpreter.)]
    (.execute (.parseQuery interpreter query) (.newConnection (ConnectionFactory.) server))))
    
(defn get-filename 
  [file]
  (substring-before (.substring (inc (.lastIndexOf file "/"))) ".xml"))
    
(defn url-from-file
  [file]
  (let [answer (execute-query (format-url-query (get-filename file)))]
    (.toString (.getObject answer 0))))
          
(defn -deleteGraph
  []
  (let [factory (ConnectionFactory.)
        conn (.newConnection factory server)
        interpreter (SparqlInterpreter.)]
      (.execute conn (DropGraph. graph))
      (.close conn)))
      
(defn -deleteUri
  [uri]
  (let [deletesub (str "construct { <" uri "> ?p ?r }
                        from <rmi://localhost/papyri.info#pi>
                        where { <" uri "> ?p ?r }")
        deleteobj (str "construct { ?s ?p <" uri ">}
                        from <rmi://localhost/papyri.info#pi>
                        where { ?s ?p <" uri ">}")]
  (let [factory (ConnectionFactory.)
        conn (.newConnection factory server)
        interpreter (SparqlInterpreter.)]
    (.execute conn (CreateGraph. graph))
    (.execute (Deletion. graph, (.parseQuery interpreter deletesub)) conn)
    (.execute (Deletion. graph, (.parseQuery interpreter deleteobj)) conn)
    (.close conn))))
    
(defn -loadFile
  [f]
  (let [factory (ConnectionFactory.)
        conn (.newConnection factory server)
        file (File. f)]
      (.execute conn (CreateGraph. graph))
      (.execute (Load. (.toURI file) graph, true) conn)
      (.close conn)))
    
(defn -insertInferences
  [url]
  (if (not (nil? url))
    (let [factory (ConnectionFactory.)
    conn (.newConnection factory server)
    interpreter (SparqlInterpreter.)]
      (.execute conn (CreateGraph. graph))
      (.execute
       (Insertion. graph,
       (.parseQuery interpreter
        (str "prefix dc: <http://purl.org/dc/terms/> "
             "construct{?s dc:hasPart <" url ">} "
             "from <rmi://localhost/papyri.info#pi> "
             "where { <" url "> dc:isPartOf ?s}"))) conn)
      (.execute
       (Insertion. graph,
       (.parseQuery interpreter
        (str "prefix dc: <http://purl.org/dc/terms/> "
             "construct{?s dc:relation <" url ">} "
             "from <rmi://localhost/papyri.info#pi> "
             "where { <" url "> dc:relation ?s}"))) conn)
      (.execute
       (Insertion. graph,
       (.parseQuery interpreter
        (str "prefix dc: <http://purl.org/dc/terms/> "
             "construct{<" url "> dc:relation ?o2} "
             "from <rmi://localhost/papyri.info#pi> "
             "where { <" url "> dc:relation ?o1 . "
             "?o1 dc:relation ?o2 "
             "filter (!sameTerm(<" url ">, ?o2))}"))) conn)
      (.close conn))
    (let [factory (ConnectionFactory.)
    conn (.newConnection factory server)
    interpreter (SparqlInterpreter.)]
      (def hasPart (str "prefix dc: <http://purl.org/dc/terms/> "
      "construct{?s dc:hasPart ?o} "
      "from <rmi://localhost/papyri.info#pi> "
      "where { ?o dc:isPartOf ?s}"))
      (def relation "prefix dc: <http://purl.org/dc/terms/> 
      construct{?s dc:relation ?o} 
      from <rmi://localhost/papyri.info#pi> 
      where { ?o dc:relation ?s}")
      (def translations "prefix dc: <http://purl.org/dc/terms/>
      construct { ?r1 <http://purl.org/dc/terms/relation> ?r2 }
      from <rmi://localhost/papyri.info#pi>
      where {
      ?i dc:relation ?r1 .
      ?i dc:relation ?r2 .
      FILTER  regex(str(?i), \"^http://papyri.info/hgv\") 
      FILTER  regex(str(?r1), \"^http://papyri.info/ddbdp\")
      FILTER  regex(str(?r2), \"^http://papyri.info/hgvtrans\")}")
      (def images "prefix dc: <http://purl.org/dc/terms/>
      construct { ?r1 <http://purl.org/dc/terms/relation> ?r2 }
      from <rmi://localhost/papyri.info#pi>
      where {
      ?c dc:isPartOf <http://papyri.info/apis> .
      ?i dc:isPartOf ?c .
      ?i dc:relation ?r1 .
      ?i dc:relation ?r2 .
      FILTER ( regex(str(?r1), \"^http://papyri.info/ddbdp\") || regex(str(?r1), \"^http://papyri.info/hgv\")) 
      FILTER  regex(str(?r2), \"^http://papyri.info/images\")}")
      (def transitive-rels 
        (if (nil? url) (str "prefix dc: <http://purl.org/dc/terms/>
                             construct{?s dc:relation ?o2}
                             from <rmi://localhost/papyri.info#pi>
                             where { ?s dc:relation ?o1 .
                                     ?o1 dc:relation ?o2 
                             filter (!sameTerm(?s, ?o2))}")
                        (str "prefix dc: <http://purl.org/dc/terms/>
                              construct{<" url "> dc:relation ?o2}
                              from <rmi://localhost/papyri.info#pi>
                              where { <" url "> dc:relation ?o1 .
                                      ?o1 dc:relation ?o2 
                              filter (!sameTerm(<" url ">, ?o2))}")
                        ))
      (.execute conn (CreateGraph. graph))
      (.execute (Insertion. graph, (.parseQuery interpreter hasPart)) conn)
      (.execute (Insertion. graph, (.parseQuery interpreter relation)) conn)
      (.execute (Insertion. graph, (.parseQuery interpreter translations)) conn)
      (.execute (Insertion. graph, (.parseQuery interpreter images)) conn)
      (.execute (Insertion. graph, (.parseQuery interpreter transitive-rels)) conn)
      (.close conn))))
      
(defn load-map 
  [file]
  (def nthreads 10)
  (let [xsl (choose-xslt file)]
    (init-xslt xsl)
    (if (.contains xsl "ddbdp-rdf") 
      (dosync (ref-set param '("root" idproot)))
      (dosync (ref-set param '("DDB-root" ddbroot)))))
      (let [factory (ConnectionFactory.)
        conn (.newConnection factory server)
        create (CreateGraph. graph)]
      (.execute conn create)
      (.close conn))
      (.. System out (println "connection completed"))
  (let [pool (Executors/newFixedThreadPool nthreads)
        files (file-seq (File. file))
        tasks (map (fn [x]
    (fn []
      (transform x)
      (if (> (count @buffer) 500)
        (flush-buffer nil))))
    (filter #(.endsWith (.getName %) ".xml") files))]
    (doseq [future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))
  (flush-buffer (count @buffer))
  )
    
(defn -mapFiles
  [files]
  (for [file files]
    (load-map file))
  )

(defn -mapAll
  [args]
  (if (> (count args) 0) 
    (load-map (first args)))
    (do 
      (println "Processing DDB_EpiDoc_XML")
      (load-map (str idproot "/DDB_EpiDoc_XML"))
      (println "Processing HGV_meta_EpiDoc")
      (load-map (str idproot "/HGV_meta_EpiDoc"))
      (println "Processing APIS")
      (load-map (str idproot "/APIS"))
      (println "Processing HGV_trans_EpiDoc")
      (load-map (str idproot "/HGV_trans_EpiDoc")))
  (-loadFile "/data/papyri.info/git/navigator/pn-mapping/sources/collection.rdf")
  (-loadFile "/data/papyri.info/git/navigator/pn-mapping/sources/apis-images.n3")
  (-loadFile "/data/papyri.info/git/navigator/pn-mapping/sources/glrt.n3")
  (-insertInferences nil)
  )

(defn -main
  [& args]
  (if (> (count args) 0)
    (let [function (first args)]
      (cond (= function "map-all") (-mapAll (rest args))
            (= function "map-files") (-mapFiles (rest args))
            (= function "load-file") (-loadFile (second args))
            (= function "delete-graph") (-deleteGraph)
            (= function "delete-uri") (-deleteUri (second args))
            (= function "insert-inferences") (if (> (count args) 1)
              (for [file (rest args)] 
                (-insertInferences (url-from-file file)))
              (-insertInferences nil))
            (= function "help") (print help)))
    ((print help))))
    
  

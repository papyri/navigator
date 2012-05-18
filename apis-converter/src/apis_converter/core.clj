;Reads APIS input files and emits XML events

(ns apis_converter.core
  (:gen-class)
  (:import (java.io BufferedReader File FileInputStream FileReader)
           (java.util.concurrent Executors ConcurrentLinkedQueue)
           (javax.xml.transform Templates)
           (javax.xml.transform.stream StreamSource StreamResult)
           (net.sf.saxon Configuration PreparedStylesheet StandardErrorListener StandardURIResolver TransformerFactoryImpl)
           (net.sf.saxon.trans CompilerInfo XPathException)
           (org.xml.sax ContentHandler Attributes SAXException)
           (org.xml.sax.helpers AttributesImpl)))

(def templates (ref nil))

(defn read-file
  [file-name]
  (line-seq (BufferedReader. (FileReader. file-name))))

(defn handle-line
    [line, elt-name, handler]
  (if (> (alength line) 2) ; lines < 2 columns long are either continuations or empty fields
    (do (let [atts (AttributesImpl.)]
        (doto atts
          (.addAttribute "" "n" "n" "CDATA" (.trim (aget line 1))))
        (if (> (alength line) 3)
          (doto atts
            (.addAttribute "" "m" "m" "CDATA" (.trim (aget line 2)))))
        (if (false? (.equals elt-name ""))
          (.endElement handler "" elt-name elt-name))
        (.startElement handler "" (aget line 0) (aget line 0) atts))
        (let [content (aget line (- (alength line) 1))]
            (.characters handler (.toCharArray (.trim content)) 0 (.length (.trim content)))))
    (do 
      (if (== (alength line) 1)
        (.characters handler (.toCharArray (aget line 0)) 0 (.length (aget line 0)))))))

(defn process-file
  [lines, elt-name, handler]
  (if (empty? lines)
    (.endElement handler "" elt-name elt-name)
      (let [line (.split (first lines) "\\s+\\|\\s+")
            ename (if (.contains (first lines) "|") (aget line 0) elt-name)]
        (if (not (.startsWith (first lines) "#"))  ; comments start with '#' and can be ignored
          (handle-line line elt-name handler))
        (process-file (rest lines) ename handler))))

(defn generate-xml
    [file-var]
  (let [xslt (.poll @templates)
        handler (.newTransformerHandler (TransformerFactoryImpl.) xslt)]
    (try
      (doto handler
        (.setResult (StreamResult. (File. (.replace (.replace (str file-var) "intake_files" "xml") ".if" ".xml"))))
        (.startDocument)
        (.startElement "" "apis" "apis" (AttributesImpl.)))
      (process-file (read-file file-var) "" handler)
      (doto handler
        (.endElement "" "apis" "apis")
        (.endDocument))
      (catch Exception e 
        (.println *err* (str (.getMessage e) " processing file " file-var))))
    (.add @templates xslt)))
      
(defn init-templates
    [xslt, nthreads]
  (dosync (ref-set templates (ConcurrentLinkedQueue.) ))
  (dotimes [n nthreads]
    (let [xsl-src (StreamSource. (FileInputStream. xslt))
            configuration (Configuration.)
            compiler-info (CompilerInfo.)]
          (doto xsl-src 
            (.setSystemId xslt))
          (doto compiler-info
            (.setErrorListener (StandardErrorListener.))
            (.setURIResolver (StandardURIResolver. configuration)))
          (dosync (.add @templates (.newTemplates (TransformerFactoryImpl.) xsl-src compiler-info))))))
  
(defn -main
  ([dir-name, xsl]
    (-main dir-name xsl 2))
  ([dir-name, xsl, nthreads]
  (def xslt xsl)
  (def dirs (file-seq (File. dir-name)))
  (init-templates xslt nthreads)
  (let [pool (Executors/newFixedThreadPool nthreads)
    tasks (map (fn [x]
        (fn []
          (generate-xml x)))
      (filter #(.endsWith (.getName %) ".if") dirs))]
      (doseq [future (.invokeAll pool tasks)]
            (.get future))
      (.shutdown pool))))



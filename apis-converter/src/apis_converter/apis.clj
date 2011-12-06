;Reads APIS input files and emits XML events

(ns info.papyri.apis
	(:gen-class)
    (:import (java.io BufferedReader File FileInputStream FileReader)
             (javax.xml.transform Templates)
             (javax.xml.transform.stream StreamSource StreamResult)
             (net.sf.saxon Configuration PreparedStylesheet StandardErrorListener StandardURIResolver TransformerFactoryImpl)
             (net.sf.saxon.trans CompilerInfo)
             (org.xml.sax ContentHandler Attributes SAXException)
             (org.xml.sax.helpers AttributesImpl)))

(def templates (ref nil))
(def xslt (ref nil))

(defn read-file
  [file-name]
  (line-seq (BufferedReader. (FileReader. file-name))))

(defn handle-line
    [line, elt-name, handler]
  (if (> (alength line) 1)
    (do (let [atts (AttributesImpl.)]
        (doto atts
          (.addAttribute "" "n" "n" "CDATA" (aget line 1)))
        (if (> (alength line) 3)
          (doto atts
            (.addAttribute "" "m" "m" "CDATA" (aget line 2))))
        (if (false? (.equals elt-name ""))
          (.endElement handler "" elt-name elt-name))
        (.startElement handler "" (aget line 0) (aget line 0) atts))
        (let [content (aget line (- (alength line) 1))]
            (.characters handler (.toCharArray content) 0 (.length content))))
    (do 
			(if (not (.startsWith (aget line 0) "#")) (.characters handler (.toCharArray (aget line 0)) 0 (.length (aget line 0)))))))

(defn process-file
  [lines, elt-name, handler]
  (if (empty? lines)
    (.endElement handler "" elt-name elt-name)
    (let [line (.split (first lines) "\\s+\\|\\s+")
            ename (if (.contains (first lines) "|") (aget line 0) elt-name)]
        (handle-line line elt-name handler)
        (process-file (rest lines) ename handler))))

(defn generate-xml
    [file-var, handler]
  (.startDocument handler)
  (.startElement handler "" "apis" "apis" (AttributesImpl.))
  (def lines (read-file file-var))
  (process-file lines "" handler)
  (.endElement handler "" "apis" "apis")
  (.endDocument handler))

(defn -main
    [file-name, xsl]
  (let [xsl-src (StreamSource. (FileInputStream. xsl))
        configuration (Configuration.)
        compiler-info (CompilerInfo.)]
    (doto xsl-src 
      (.setSystemId xsl))
    (doto compiler-info
      (.setErrorListener (StandardErrorListener.))
      (.setURIResolver (StandardURIResolver. configuration)))
    (dosync (ref-set templates (TransformerFactoryImpl. configuration)))
    (dosync (ref-set xslt xsl-src)))
  (let [handler (.newTransformerHandler @templates @xslt)]
    (doto handler
      (.setResult (StreamResult. (File. (str file-name ".xml")))))
			(print (str file-name ".xml"))
    (generate-xml file-name handler)))

(-main  (second *command-line-args*) (last *command-line-args*))




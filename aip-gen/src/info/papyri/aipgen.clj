(ns info.papyri.aipgen
(:import (java.io File FileInputStream FileWriter)
         (java.util Date)
         (java.security MessageDigest)
         (java.text SimpleDateFormat)
         (org.apache.commons.codec.digest DigestUtils))
  (:use (clojure.data xml)))
  
(def base "/data/papyri.info/packages/AIP")
(def package-dir (ref nil))
(def id-counter (atom 0))

(defn emit-fragment
  "Prints the given Element tree as XML text to stream.
   Options:
    :encoding <str>          Character encoding to use"
  [e ^java.io.Writer stream & {:as opts}]
  (let [^javax.xml.stream.XMLStreamWriter writer (-> (javax.xml.stream.XMLOutputFactory/newInstance)
                                                     (.createXMLStreamWriter stream))]

    (when (instance? java.io.OutputStreamWriter stream)
      (check-stream-encoding stream (or (:encoding opts) "UTF-8")))
      (doseq [event (flatten-elements [e])]
      (emit-event event writer))
    stream))

(defn lpad
  [string length]
  (if (< (count string) length)
    (let [s (StringBuilder.)]
      (dotimes [n (- length (count string))]
        (.append s "0"))
      (str s string))
    string))
    
(defn substring-after
  "Returns the part of string1 that comes after the first occurrence of string2, or 
  nil if string1 does not contain string2."
  [string1 string2]
  (when (.contains string1 string2) (.substring string1 (+ (.indexOf string1 string2) (.length string2)))))
    
(defn sha1
  [file]
  (let [dg (DigestUtils/sha1Hex (FileInputStream. file))]
    (.toString dg)))

(defn files
  [dir]
  (let [files (file-seq (File. (str base "/" @package-dir "/" dir)))]
    (for [file (filter (fn [f] (not (.isDirectory f))) files)]
        (element :mets:file {:ID (str dir "-" (lpad (str (swap! id-counter inc)) 6)), :CHECKSUM (sha1 file), 
                             :CHECKSUMTYPE "SHA-1"}
          (element :mets:FLocat {:LOCTYPE "URL" :xlink:type "simple" 
                   :xlink:href (substring-after (.getPath file) base)})))))

(defn mets 
  [id, out]
  (let [df (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ")
        date (Date.)]
    (indent (element :mets:mets {:OBJID id, :TYPE "Text", :xmlns:mets "http://www.loc.gov/METS/", 
        :xmlns:xlink "http://www.w3.org/1999/xlink", :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance",
        :xsi:schemaLocation "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/version191/mets.xsd",
        :xmlns:aipTemplateVersion "info:nyu/dl/v1.0/templates/aip/v1.0.0"}
      (element :mets:metsHdr {:CREATEDATE (.format df date), :LASTMODDATE (.format df date), :RECORDSTATUS "Completed"}
        (element :mets:agent {:ROLE "CREATOR", :TYPE "INDIVIDUAL"} "Cayless, Hugh")
        (element :mets:agent {:ROLE "DISSEMINATOR", :TYPE "ORGANIZATION"} "NYU Digital Library Technology Services"))
      (element :mets:fileSec {}
        (element :mets:fileGrp {:ADMID "git-md"}
          (files "git"))
        (element :mets:fileGrp {:ADMID "html-md"}
          (files "html"))
        (element :mets:fileGrp {:ADMID "text-md"}
          (files "text"))
        (element :mets:fileGrp {:ADMID "ddb-md"}
          (files "xml/DDB_EpiDoc_XML")
          (files "xml/RDF"))
        (element :mets:fileGrp {:ADMID "apis-md"}
          (files "xml/APIS"))
        (element :mets:fileGrp {:ADMID "hgv-md"}
          (files "xml/HGV_meta_EpiDoc")
          (files "xml/HGV_metadata")
          (files "xml/HGV_trans_EpiDoc"))
        (element :mets:fileGrp {:ADMID "biblio-md"}
          (files "xml/Biblio"))
        (element :mets:fileGrp {:ADMID "rdf-md"}
          (files "rdf")))) out)))
        
(defn -main
  "I don't do a whole lot."
  [& args]
  (dosync (ref-set package-dir (first args)))
  (with-open [out (java.io.FileWriter. "test.xml")]
    (mets "TEST-kprr5gn" out)))
  

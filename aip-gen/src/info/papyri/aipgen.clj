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
(def lists {:git (ref (ConcurrentSkipListMap.)), :html (ref (ConcurrentSkipListMap.)), 
  :text (ref (ConcurrentSkipListMap.)), :xml (ref (ConcurrentSkipListMap.)), :rdf (ref (ConcurrentSkipListMap.))})
(def current-list (ref nil))

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
  (with-open [fis (FileInputStream. file)]
    (.toString (DigestUtils/sha1Hex fis))))

(defn fileSec
  [id, dir]
  (let [files (file-seq (File. (str base "/" @package-dir "/" dir)))
        ls (deref (id lists))]
    (for [file (filter (fn [f] (not (.isDirectory f))) files)
          :let [fid (lpad (str (swap! id-counter inc)) 8)]]
        (.put ls (.getPath file) (str id "-" fid))
        (element :mets:file {:ID (str id "-" fid), :CHECKSUM (sha1 file), 
                             :CHECKSUMTYPE "SHA-1"}
          (element :mets:FLocat {:LOCTYPE "URL" :xlink:type "simple" 
                   :xlink:href (substring-after (.getPath file) (str base "/")})))))
                   
(defn structMap
  [file]
  (if (.isDirectory file)
    (element :mets:div {:LABEL (.getName file)}
      (for [f (.listFiles file)]
        (structMap file)))
    (element :mets:fptr {:FILEID ((.getPath file)(deref (@current-list lists)))})))

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
          (dosync (ref-set current-list :git))
          (files :git "git"))
        (element :mets:fileGrp {:ADMID "html-md"}
          (dosync (ref-set current-list :html))
          (reset! id-counter 0)
          (files :html "html"))
        (element :mets:fileGrp {:ADMID "text-md"}
          (dosync (ref-set current-list :text))
          (reset! id-counter 0)
          (files :text "text"))
        (element :mets:fileGrp {:ADMID "ddb-md"}
          (dosync (ref-set current-list :xml))
          (reset! id-counter 0)
          (files :xml "xml/DDB_EpiDoc_XML")
          (files :xml "xml/RDF"))
        (element :mets:fileGrp {:ADMID "apis-md"}
          (files :xml "xml/APIS"))
        (element :mets:fileGrp {:ADMID "hgv-md"}
          (files :xml "xml/HGV_meta_EpiDoc")
          (files :xml "xml/HGV_metadata")
          (files :xml "xml/HGV_trans_EpiDoc"))
        (element :mets:fileGrp {:ADMID "biblio-md"}
          (files :xml "xml/Biblio"))
        (element :mets:fileGrp {:ADMID "rdf-md"}
          (dosync (ref-set current-list :rdf))
          (reset! id-counter 0)
          (files :rdf "rdf"))
        (element :mets:structMap {:TYPE "PHYSICAL"}
          (element :mets:div {}
            (element :mets:div {:ORDER "1"}
              (structMap (File. (str base "/files/data/git"))))
            (element :mets:div {:ORDER "2"}
              (structMap (File. (str base "/files/data/html"))))
            (element :mets:div {:ORDER "3"}
              (structMap (File. (str base "/files/data/text"))))    
            (element :mets:div {:ORDER "4"}
              (structMap (File. (str base "/files/data/xml")))) 
            (element :mets:div {:ORDER "5"}
              (structMap (File. (str base "/files/data/rdf")))))))) out)))
        
(defn -main
  "I don't do a whole lot."
  [& args]
  (dosync (ref-set package-dir (first args)))
  (with-open [out (java.io.FileWriter. "test.xml")]
    (mets "TEST-kprr5gn" out)))
  

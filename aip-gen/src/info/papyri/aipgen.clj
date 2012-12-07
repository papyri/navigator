(ns info.papyri.aipgen
(:import (java.io File FileInputStream FileWriter)
         (java.util Date)
         (java.util.concurrent ConcurrentSkipListMap)
         (java.security MessageDigest)
         (java.text SimpleDateFormat)
         (org.apache.commons.codec.digest DigestUtils))
  (:use (clojure.data xml)))
  
(def base "/data/papyri.info/packages/AIP")
(def package-dir (ref nil))
(def id-counters {:git (atom 0), :html (atom 0), :text (atom 0), :xml (atom 0), :rdf (atom 0)})
(def lists {:git (ref (ConcurrentSkipListMap.)), :html (ref (ConcurrentSkipListMap.)), 
  :text (ref (ConcurrentSkipListMap.)), :xml (ref (ConcurrentSkipListMap.)), :rdf (ref (ConcurrentSkipListMap.))})
(def current-list (ref nil))

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
  (let [files (file-seq (File. (str base "/files/data/" @package-dir "/" dir)))
        ls (deref (get lists id))
        id-counter (get id-counters id)]
    (for [file (filter (fn [f] (not (.isDirectory f))) files)
          :let [fid (lpad (str (swap! id-counter inc)) 8)]]
        (element :mets:file {:ID (str (name id) "-" fid), :CHECKSUM (sha1 file), 
                             :CHECKSUMTYPE "SHA-1"}
          (do (.put ls (.getPath file) (str (name id) "-" fid))
          (element :mets:FLocat {:LOCTYPE "URL" :xlink:type "simple" 
                   :xlink:href (substring-after (.getPath file) (str base "/"))}))))))
                   
(defn structMap
  [id, file]
  (if (.isDirectory file)
    (element :mets:div {:LABEL (.getName file)}
      (for [f (.listFiles file)]
        (structMap id f)))
    (element :mets:fptr {:FILEID (.get (deref (get lists id)) (.getPath file))})))

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
          (fileSec :git "git"))
        (element :mets:fileGrp {:ADMID "html-md"}
          (do 
            (reset! id-counter 0)
            (fileSec :html "html")))
        (element :mets:fileGrp {:ADMID "text-md"}
          (do 
            (reset! id-counter 0)
            (fileSec :text "text")))
        (element :mets:fileGrp {:ADMID "ddb-md"}
          (do 
            (reset! id-counter 0)
            (fileSec :xml "xml/DDB_EpiDoc_XML"))
          (fileSec :xml "xml/RDF"))
        (element :mets:fileGrp {:ADMID "apis-md"}
          (fileSec :xml "xml/APIS"))
        (element :mets:fileGrp {:ADMID "hgv-md"}
          (fileSec :xml "xml/HGV_meta_EpiDoc")
          (fileSec :xml "xml/HGV_metadata")
          (fileSec :xml "xml/HGV_trans_EpiDoc"))
        (element :mets:fileGrp {:ADMID "biblio-md"}
          (fileSec :xml "xml/Biblio"))
        (element :mets:fileGrp {:ADMID "rdf-md"}
          (do 
            (reset! id-counter 0)
            (fileSec :rdf "rdf"))))
      (element :mets:structMap {:TYPE "PHYSICAL"}
        (element :mets:div {}
          (element :mets:div {:ORDER "1"}
            (structMap :git File. (str base "/files/data/" @package-dir "/git")))
          (element :mets:div {:ORDER "2"}
            (structMap :html File. (str base "/files/data/" @package-dir "/html")))
          (element :mets:div {:ORDER "3"}
            (structMap :text File. (str base "/files/data/" @package-dir "/text")))
          (element :mets:div {:ORDER "4"}
            (structMap :xml File. (str base "/files/data/" @package-dir "/xml")))
          (element :mets:div {:ORDER "5"}
            (structMap :rdf File. (str base "/files/data/" @package-dir "/rdf")))))) 
      out)))
        
(defn -main
  "I don't do a whole lot."
  [& args]
  (dosync (ref-set package-dir (first args)))
  (with-open [out (java.io.FileWriter. "test.xml")]
    (mets "TEST-kprr5gn" out)))
  

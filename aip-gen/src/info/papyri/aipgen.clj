(ns info.papyri.aipgen
  (:import (java.io File FileInputStream FileWriter)
         (java.util Date)
         (java.util.concurrent ConcurrentSkipListMap)
         (java.security MessageDigest)
         (java.text SimpleDateFormat)
         (org.apache.commons.codec.digest DigestUtils))
  (:use (clojure.data xml)
        (clojure.java io)))

(def base "/Volumes/LaCie-R/papyri.info/AIP")
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
  [id, package-id, dir]
  (let [files (file-seq (File. (str base "/" package-id "/files/" @package-dir "/data/" dir)))
        ls (deref (get lists id))
        id-counter (get id-counters id)]
    (for [file (filter (fn [f] (and (.exists f) (not (.isDirectory f)))) files)
          :let [fid (lpad (str (swap! id-counter inc)) 8)]]
        (element :mets:file {:ID (str (name id) "-" fid), :CHECKSUM (sha1 file),
                             :CHECKSUMTYPE "SHA-1"}
          (do (.put ls (.getPath file) (str (name id) "-" fid))
          (element :mets:FLocat {:LOCTYPE "URL" :xlink:type "simple"
                   :xlink:href (str "files/" (substring-after (.getPath file) "files/"))}))))))

(defn structMap
  [id, file]
  (if (.isDirectory file)
    (element :mets:div {:LABEL (.getName file)}
      (for [f (.listFiles file)]
        (structMap id f)))
    (element :mets:div {:LABEL (.replaceFirst (.getName file) "\\.\\w+$" "")}
      (element :mets:fptr {:FILEID (.get (deref (get lists id)) (.getPath file))}))))

(defn mets
  [id, out]
  (let [df (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss")
        date (Date.)]
    (indent (element :mets:mets {:OBJID id, :TYPE "Text", :xmlns:mets "http://www.loc.gov/METS/",
        :xmlns:xlink "http://www.w3.org/1999/xlink", :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance",
        :xmlns:dc "http://purl.org/dc/terms/",
        :xsi:schemaLocation "http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/version191/mets.xsd",
        :xmlns:aipTemplateVersion "info:nyu/dl/v1.0/templates/aip/v1.0.0"}
      (element :mets:metsHdr {:CREATEDATE (.format df date), :LASTMODDATE (.format df date), :RECORDSTATUS "Completed"}
        (element :mets:agent {:ROLE "CREATOR", :TYPE "INDIVIDUAL"}
          (element :mets:name {} "Cayless, Hugh"))
        (element :mets:agent {:ROLE "DISSEMINATOR", :TYPE "ORGANIZATION"}
          (element :mets:name {} "NYU Digital Library Technology Services")))
        (element :mets:dmdSec {:ID "git-dmd"}
          (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                :xlink:href (str "files/" @package-dir "/metadata/git-dmd.xml"),
                                :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/git-dmd.xml")))
                                :CHECKSUMTYPE "SHA-1"}))
        (element :mets:dmdSec {:ID "html-dmd"}
          (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                :xlink:href (str "files/" @package-dir "/metadata/html-dmd.xml"),
                                :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/html-dmd.xml")))
                                :CHECKSUMTYPE "SHA-1"}))
        (element :mets:dmdSec {:ID "text-dmd"}
          (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                :xlink:href (str "files/" @package-dir "/metadata/text-dmd.xml"),
                                :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/text-dmd.xml")))
                                :CHECKSUMTYPE "SHA-1"}))
        (element :mets:dmdSec {:ID "xml-dmd"}
          (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                :xlink:href (str "files/" @package-dir "/metadata/xml-dmd.xml"),
                                :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/xml-dmd.xml")))
                                :CHECKSUMTYPE "SHA-1"}))
        (element :mets:dmdSec {:ID "rdf-dmd"}
          (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                :xlink:href (str "files/" @package-dir "/metadata/rdf-dmd.xml"),
                                :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/rdf-dmd.xml")))
                                :CHECKSUMTYPE "SHA-1"}))
        (element :mets:amdSec {:ID "amd_001"}
          (element :mets:rightsMD {:ID "git-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/git-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/git-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "html-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/html-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/html-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "text-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/text-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/git-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "ddb-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/ddb-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/ddb-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "apis-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/apis-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/apis-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "hgv-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/hgv-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/hgv-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "biblio-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/biblio-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/biblio-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"}))
          (element :mets:rightsMD {:ID "rdf-rmd"}
            (element :mets:mdRef {:LOCTYPE "URL", :xlink:type "simple",
                                  :xlink:href (str "files/" @package-dir "/metadata/rdf-rmd.xml"),
                                  :MDTYPE "METSRIGHTS" :CHECKSUM (sha1 (File. (str base "/" id "/files/" @package-dir "/metadata/rdf-rmd.xml")))
                                  :CHECKSUMTYPE "SHA-1"})))
      (element :mets:fileSec {}
        (element :mets:fileGrp {:ADMID "git-dmd git-rmd"}
          (fileSec :git id "git"))
        (element :mets:fileGrp {:ADMID "html-dmd html-rmd"}
          (fileSec :html id "html"))
        (element :mets:fileGrp {:ADMID "text-dmd text-rmd"}
          (fileSec :text id "text"))
        (element :mets:fileGrp {:ADMID "xml-dmd ddb-rmd"}
          (fileSec :xml id "xml/DDB_EpiDoc_XML")
          (fileSec :xml id "xml/RDF"))
        (element :mets:fileGrp {:ADMID "xml-dmd apis-rmd"}
          (fileSec :xml id "xml/APIS"))
        (element :mets:fileGrp {:ADMID "xml-dmd hgv-rmd"}
          (fileSec :xml id "xml/HGV_meta_EpiDoc")
          (fileSec :xml id "xml/HGV_metadata")
          (fileSec :xml id "xml/HGV_trans_EpiDoc"))
        (element :mets:fileGrp {:ADMID "xml-dmd biblio-rmd"}
          (fileSec :xml id "xml/Biblio"))
        (element :mets:fileGrp {:ADMID "rdf-dmd rdf-rmd"}
          (fileSec :rdf id "rdf")))
      (element :mets:structMap {:TYPE "PHYSICAL"}
        (element :mets:div {}
          (element :mets:div {:ORDER "1"}
            (structMap :git (File. (str base "/" id "/files/" @package-dir "/data/git"))))
          (element :mets:div {:ORDER "2"}
            (structMap :html (File. (str base "/" id "/files/" @package-dir "/data/html"))))
          (element :mets:div {:ORDER "3"}
            (structMap :text (File. (str base "/" id "/files/" @package-dir "/data/text"))))
          (element :mets:div {:ORDER "4"}
            (structMap :xml (File. (str base "/" id "/files/" @package-dir "/data/xml"))))
          (element :mets:div {:ORDER "5"}
            (structMap :rdf (File. (str base "/" id "/files/" @package-dir "/data/rdf")))))))
      out)))

(defn -main
  [& args]
  (dosync (ref-set package-dir (second args)))
  (with-open [out (java.io.FileWriter. (str base "/" (first args) "/" (first args) "-" @package-dir ".xml"))]
    (mets (first args) out)))


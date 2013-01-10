(ns image2rdf.core
  (:import 
    (com.hp.hpl.jena.rdf.model Model ModelFactory Resource ResourceFactory)
    (com.hp.hpl.jena.datatypes.xsd XSDDatatype)
    (org.apache.commons.codec.digest DigestUtils)
    (java.io File FileOutputStream)
    (java.util Comparator)
    (java.security MessageDigest)))

(defn create-resource
  ([]
   (ResourceFactory/createResource))
  ([uri]
  (ResourceFactory/createResource uri)))

(defn create-property
  ([uri]
  (ResourceFactory/createProperty uri))
  ([namespace local]
   (ResourceFactory/createProperty namespace local)))

(defn create-typed-literal
 [literal, type]
 (ResourceFactory/createTypedLiteral literal type))

(defn create-plain-literal
  [literal]
  (ResourceFactory/createPlainLiteral literal))

(defn create-statement
  [subject predicate object]
  (ResourceFactory/createStatement subject predicate object))

(defn create-seq
  ([model]
    (.createSeq model))
  ([model uri]
    (.createSeq model uri))) 

(defn create-model
  []
  (ModelFactory/createDefaultModel))
  
(defn get-prefixes
  "Given a list of file names, boil it down to a list of prefixes which
   match the given pattern"
  [files pattern]
  (reduce 
    (fn [names file]
      (let [*matcher* (re-matcher pattern (.getName file))
            prefix (re-find *matcher*)]
        (if (= (last names) prefix)
          names
          (conj names prefix))))
    []
    files))
    
(defn rpad
  [s length ch]
  (if (< (count s) length)
    (let [r (StringBuilder.)]
      (dotimes [n (- length (count s))]
        (.append r ch))
      (str s r))
    s))
	
;; we want order: recto, verso, which is often simple sort order,
;; but APIS images are sometimes labeled "f(ront)", "b(ack)"
(def compt 
  (proxy [Comparator] []
    (compare [o1 o2]
      (let [no1 (.replaceAll o1 "\\.b\\." ".v.")
            no2 (.replaceAll o2 "\\.b\\." ".v.")
            pattern #"([^.]+)\.(apis).([^.]+).(f|v)\.([^.]+).([^.]+)"]
        (if (and (re-find pattern no1) (re-find pattern no2))
          (let [m1 (re-matcher pattern no1)
                m2 (re-matcher pattern no2)]
            (re-find m1)
            (re-find m2)
            (let [g1 (re-groups m1)
                  g2 (re-groups m2)]
              (.compareTo 
                (str (nth g1 1) "." (nth g1 2) "." (nth g1 3) "." (rpad (nth g1 5) 3 "-") "." (nth g1 4) "." (last g1))
                (str (nth g2 1) "." (nth g2 2) "." (nth g2 3) "." (rpad (nth g2 5) 3 "-") "." (nth g2 4) "." (last g2)))))
          (.compareTo no1 no2))))))

(defn image-dir
  [filename]
  (let [md (DigestUtils/md5Hex (str filename "\n"))]
    (.substring (.toString md) 0 2)))
    
(defn image-name
  [file]
  (.substring (.getName file) 0 (.lastIndexOf (.getName file )".")))

(defn -main 
  "Takes a directory path, a pattern to match file prefixes against, and a URI prefix."
  [& args]
  (let [model (create-model)
        d (File. (first args))
        up (nth args 2)
        files (sort-by (fn [f] 
                          (.getName f))
                        compt
                        (rest (file-seq d)))
        prefixes (get-prefixes files (re-pattern (second args)))]
    (doseq [prefix prefixes]
      (let [apisrec (str up "/" prefix)
            images (create-resource (str up "/" prefix "/images"))
            source (create-resource (str up "/" prefix "/source"))
            matching-files (filter (fn [item] 
                                     (and (.startsWith (.getName item) prefix) (.matches (.replace (.getName item) prefix "") "^\\D.*")))
                                   files)]
        (.add model 
          (create-statement
            images
            (create-property "http://purl.org/dc/terms/relation")
            source))
        (.add model
          (create-statement
            images
            (create-property "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
            (create-resource "http://purl.org/ontology/olo/core#OrderedList")))
        (.add model
          (create-statement
            images
            (create-property "http://purl.org/ontology/olo/core#length")
            (create-typed-literal (str (count matching-files)) (XSDDatatype/XSDinteger))))
        (.add model 
          (create-statement
            source
            (create-property "http://purl.org/dc/terms/relation")
            images))
        (let [i (atom 0)]
          (doseq [li matching-files]
            (let [image (create-resource (str "http://papyri.info/images/" (image-dir (image-name li)) "/" (image-name li)))
                  imgindex (create-resource (str up "/" prefix "/images/" (swap! i inc)))]
              (.add model
                (create-statement
                  images
                  (create-property "http://purl.org/ontology/olo/core#slot")
                  imgindex))
              (.add model
                (create-statement
                  imgindex
                  (create-property "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
                  (create-resource "http://purl.org/ontology/olo/core#Slot")))
              (.add model
                (create-statement
                  imgindex
                  (create-property "http://purl.org/ontology/olo/core#item")
                  image))
              (.add model
                (create-statement
                  imgindex
                  (create-property "http://purl.org/ontology/olo/core#index")
                  (create-typed-literal (str @i) (XSDDatatype/XSDinteger))))
              (when (.matches apisrec "^http://papyri\\.info/apis/\\w+\\.apis\\.(?:\\w|\\d)+$")
                (.add model 
                  (create-statement 
                    image 
                    (create-property "http://xmlns.com/foaf/0.1/depicts") 
                    (create-resource (str apisrec "/original")))))
              (.add model
                (create-statement 
                  image 
                  (create-property "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") 
                  (create-resource "http://purl.org/ontology/bibo/Image")))
              (when (or (.contains (image-name li) ".f.") (.endsWith (image-name li) "r"))
                (.add model (create-statement
                  image
                  (create-property "http://www.w3.org/2000/01/rdf-schema#label")
                  (create-plain-literal "Recto"))))
              (when (or (.contains (image-name li) ".b.") (.matches (image-name li) ".*[^c]v$"))
                (.add model (create-statement
                  image
                  (create-property "http://www.w3.org/2000/01/rdf-schema#label")
                  (create-plain-literal "Verso"))))
              (when (.endsWith (image-name li) "cc")
                (.add model (create-statement
                  image
                  (create-property "http://www.w3.org/2000/01/rdf-schema#label")
                  (create-plain-literal "Concave"))))
              (when (.endsWith (image-name li) "cv")
                (.add model (create-statement
                  image
                  (create-property "http://www.w3.org/2000/01/rdf-schema#label")
                  (create-plain-literal "Convex")))))))))
    (.write model (FileOutputStream. (File. (last args))))))







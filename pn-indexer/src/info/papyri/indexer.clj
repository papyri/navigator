;; ## PN Indexer
;; The indexer is responsible for generating most of the Papyrological Navigator.
;; It loads data into the Solr indexes (for data, bibliography, and lemmas), and generates
;; HTML and text representations of the data. When compiled into a JAR, it exposes some
;; static methods which allow its functions to be called from Java code.
;;
;; Most of the work in PN Indexer is done by queueing up (via a query on the Numbers Server)
;; first the DDbDP data with any matching HGV and APIS records, then HGV data with any matching
;; APIS records but without any matching DDbDP record, then APIS without any DDbDP or HGV
;; matches. This queue of URIs is then processed. URIs are resolved to filenames, and those XML
;; files are transformed using XSLT into Solr add documents, HTML files, and text files.
;;
;; ### Usage
;; (from Leiningen)
;;
;; * run without arguments — builds the PN index (pn-search) and HTML/txt pages for texts and data
;; * run with a list of files — indexes and generates HTML/txt for just the files provided
;; * `biblio` — builds the PN index for bibliography (biblio-search)
;; * `load-lemmas` — loads the morphological data from the Perseus lemma db into the lemma index (morph-search)
;;
;; (from Java)
;;
;; * `info.papyri.indexer.index(List<String> files)` (files can be null)
;; * `info.papyri.indexer.loadBiblio()`
;; * `info.papyri.indexer.loadLemmas()`

(ns info.papyri.indexer
  (:gen-class
   :name info.papyri.indexer
   :methods [#^{:static true} [index [] void]
             #^{:static true} [generatePages [java.util.List] void]
             #^{:static true} [loadBiblio [] void]
             #^{:static true} [loadLemmas [] void]])
  (:require
    [clojure.java.io :as io]
    [clojure.string :as st])
  (:import
    (clojure.lang ISeq)
    (org.apache.jena.rdf.model Model ModelFactory Resource ResourceFactory)
    (java.io File FileInputStream FileOutputStream FileReader ObjectInputStream ObjectOutputStream PushbackReader StringWriter FileWriter)
    (java.net URI URL URLEncoder URLDecoder)
    (java.nio.charset Charset)
    (java.text Normalizer Normalizer$Form)
    (java.util ArrayList TreeMap)
    (java.util.concurrent Executors Future ConcurrentLinkedQueue ConcurrentSkipListSet)
    (javax.xml.parsers SAXParserFactory)
    (javax.xml.transform Result )
    (javax.xml.transform.sax SAXResult)
    (javax.xml.transform.stream StreamSource StreamResult)
    (net.sf.saxon.s9api Destination Processor QName SAXDestination Serializer XdmAtomicValue XsltCompiler XsltExecutable)
    (net.sf.saxon.lib AugmentedSource ParseOptions)
    (org.apache.solr.client.solrj SolrClient SolrQuery)
    (org.apache.solr.client.solrj.impl ConcurrentUpdateSolrClient ConcurrentUpdateSolrClient$Builder BinaryRequestWriter HttpSolrClient HttpSolrClient$Builder)
    (org.apache.solr.client.solrj.request RequestWriter)
    (org.apache.solr.common SolrInputDocument)
    (org.apache.jena.sparql.exec.http QueryExecutionHTTP)
    (org.xml.sax InputSource)
    (org.xml.sax.helpers DefaultHandler)))

;; NOTE: hard-coded paths and addresses
(def filepath "/srv/data/papyri.info/idp.data")
(def tmpath "/srv/data/papyri.info/TM")
(def xsltpath "/srv/data/papyri.info/git/navigator/pn-xslt")
(def htpath "/srv/data/papyri.info/pn/idp.html")
(def solrurl "http://localhost:8983/solr/")
(def nthreads (.availableProcessors (Runtime/getRuntime)))
(def server "http://localhost:8090/pi")
(def nserver "localhost")
(def collections (ref (ConcurrentLinkedQueue.)))
(def htmltemplates (ref nil))
(def html (ref (ConcurrentLinkedQueue.)))
(def solrtemplates (ref nil))
(def text (ref (ConcurrentLinkedQueue.)))
(def texttemplates (ref nil))
(def bibsolrtemplates (ref nil))
(def bibhtmltemplates (ref nil))
(def links (ref (ConcurrentLinkedQueue.)))
(def words (ref (ConcurrentSkipListSet.)))
(def solr (ref nil))
(def solrbiblio (ref nil))
(def ^:dynamic *current*)
(def ^:dynamic *doc*)
(def ^:dynamic *index*)
(def processor (Processor. false))

(defn add-words
  "Adds the list of words provided to the set in the ref `words`."
  [words]
  (let [word-arr (.split words "\\s+")]
    (for [word word-arr]
      (.add @words word))))

(defn dochandler
  "A document handler for Solr that handles the conversion of a SOLR XML
  document into a Java object representing an add document and then when
  the document has been read to its end, adds that document to the
  `ConcurrentUpdateSolrClient` stored in the @solr ref."
  []
  (SAXDestination.
    (let [current (StringBuilder.)
          chars  (StringBuilder.)
          solrdoc (SolrInputDocument. (make-array String 0))]
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
          (.add @solr solrdoc))))))

(defn bibliodochandler
  "A document handler that behaves much like `dochandler` above, but
  works for bibliographic records."
  []
     (SAXDestination.
      (let [current (StringBuilder.)
	    chars  (StringBuilder.)
	    solrdoc (SolrInputDocument. (make-array String 0))]
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
		       (.add @solrbiblio solrdoc))))))

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
  "Initializes the XSLT template pool."
    [xslt, nthreads, pool]
  (dosync (ref-set (load-string pool) (ConcurrentLinkedQueue.) ))
  (dotimes [n nthreads]
    (let [xsl-src (StreamSource. (FileInputStream. xslt))
          processor (Processor. false)
          compiler (.newXsltCompiler processor)]
          (doto xsl-src
            (.setSystemId xslt))
          (let [axsl (AugmentedSource. xsl-src (ParseOptions.))]
            (doto axsl
              (.setXIncludeAware true))
            (dosync (.add (load-string (str "@" pool)) (.compile compiler axsl)))))))

;; ## Utility functions

(defn substring-after
  "Returns the part of string1 that comes after the first occurrence of string2, or
  nil if string1 does not contain string2."
  [string1 string2]
  (when (.contains string1 string2) (.substring string1 (+ (.indexOf string1 string2) (.length string2)))))

(defn substring-before
  "Returns the part of string1 that comes before the first occurrence of string2, or
  nil if string1 does not contain string2."
  [string1 string2]
  (when (.contains string1 string2) (.substring string1 0 (.indexOf string1 string2))))

(defn ceil
  [num]
  (substring-before (str (Math/ceil num)) "."))

(defn encode-url
  "Wrapper for the `java.net.URLEncoder.encode()` method."
  [url]
  (URLEncoder/encode url "UTF-8"))

(defn decode-url
  "Wrapper for the `java.net.URLEncoder.decode()` method."
  [url]
  (URLDecoder/decode url "UTF-8"))

(defn get-filename
  "Resolves the filename of the local XML file associated with the given URL."
  [url]
  (let [result (try (if (.contains url "ddbdp/")
    (let [identifier (.split (substring-before (substring-after url "http://papyri.info/ddbdp/") "/source") ";")]
      (if (= (second identifier) "")
        (str filepath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "."
       (.replace (.replace (decode-url (last identifier)) "," "-") "/" "_") ".xml")
        (str filepath "/DDB_EpiDoc_XML/" (first identifier) "/" (first identifier) "." (second identifier)
       "/" (first identifier) "." (second identifier) "."
       (.replace (.replace (decode-url (last identifier)) "," "-") "/" "_") ".xml")))
    (if (.contains url "hgv/")
      (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
            id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
        (str filepath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".xml"))
      (if (.contains url "dclp/")
        (let [identifier (substring-before (substring-after url "http://papyri.info/dclp/") "/source")
            id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
            (str filepath "/DCLP/" (ceil (/ id-int 1000)) "/" identifier ".xml"))
        (when (.contains url "apis/")
          (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
            (str filepath "/APIS/" (first identifier) "/xml/" (first identifier) "." (second identifier) "." (last identifier) ".xml"))))))
    (catch Exception e
      (when-not (nil? e)
        (println (str (.getMessage e) " processing " url ".")
        (.printStackTrace e)))))]
    (if (and result (.exists (File. result)))
      result
      (println (str result " does not exist.")))))

(defn get-txt-filename
  "Resolves the filename of the local text file associated with the given URL."
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
      (if (.contains url "/hgv/")
        (when (.endsWith url "/source")
          (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
                id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
            (str htpath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".txt")))
        (if (.contains url "/dclp/")
          (when (.endsWith url "/source")
            (let [identifier (substring-before (substring-after url "http://papyri.info/dclp/") "/source")
                id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
              (str htpath "/DCLP/" (ceil (/ id-int 1000)) "/" identifier ".txt")))
        (when (.contains url "/apis")
          (if (.endsWith url "/source")
            (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
              (str htpath "/APIS/" (first identifier) "/" (first identifier) "." (second identifier) "." (last identifier) ".txt"))))))))
       (catch Exception e
         (when-not (nil? e)
           (println (str (.getMessage e) " processing " url ".")
           (.printStackTrace e))))))


(defn get-html-filename
  "Resolves the filename of the local HTML file associated with the given URL."
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
    (if (.contains url "/hgv/")
      (if (.endsWith url "/source")
        (let [identifier (substring-before (substring-after url "http://papyri.info/hgv/") "/source")
              id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
          (str htpath "/HGV_meta_EpiDoc/HGV" (ceil (/ id-int 1000)) "/" identifier ".html"))
        (if (= url "http://papyri.info/hgv")
          (str htpath "/HGV_meta_EpiDoc/index.html")
          (str htpath "/HGV_meta_EpiDoc/" (substring-after url "http://papyri.info/hgv/") "/index.html")))
      (if (.contains url "/dclp/")
        (if (.endsWith url "/source")
          (let [identifier (substring-before (substring-after url "http://papyri.info/dclp/") "/source")
              id-int (Integer/parseInt (.replaceAll identifier "[a-z]" ""))]
            (str htpath "/DCLP/" (ceil (/ id-int 1000)) "/" identifier ".html"))
          (if (= url "http://papyri.info/dclp")
            (str htpath "/DCLP/index.html")
            (str htpath "/DCLP/" (substring-after url "http://papyri.info/dclp/") "/index.html")))
      (when (.contains url "/apis")
        (if (.endsWith url "/source")
          (let [identifier (.split (substring-before (substring-after url "http://papyri.info/apis/") "/source") "\\.")]
            (str htpath "/APIS/" (first identifier) "/" (first identifier) "." (second identifier) "." (last identifier) ".html"))
            (if (= url "http://papyri.info/apis")
              (str htpath "/APIS/index.html")
              (str htpath "/APIS/" (substring-after url "http://papyri.info/apis/") "/index.html"))))))))
    (catch Exception e
       (when-not (nil? e)
         (println (str (.getMessage e) " processing " url ".")
         (.printStackTrace e))))))

(defn transform
  "Runs an XSLT transform on the `java.io.File` in the first parameter,
  using a list of key/value parameter pairs, and feeds the result of the transform into
  a `javax.xml.transform.Result`."
  [url, params, #^Destination out, pool]
    (let [xslt (.poll pool)
    transformer (.load xslt)]
      (try
        (when (not (== 0 (count params)))
          (doseq [param params] (doto transformer
            (.setParameter (QName. (first param)) (XdmAtomicValue. (second param))))))
        (doto transformer
          (.setSource (StreamSource. (.openStream (URL. url))))
          (.setDestination out))
        (.transform transformer)
        (catch Exception e
          (println (str (.getMessage e) " transforming " url "."))
          (.printStackTrace e))
        (finally
          (.add pool xslt)))))

;; ## SPARQL Queries
;; Each of the following functions formats a SPARQL query.

(defn has-part-query
  "Constructs a set of triples where A `<dct:hasPart>` B."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a
            from <http://papyri.info/graph>
            where { <%s> dct:hasPart ?a
            filter not exists {?a dct:isReplacedBy ?b }}" url ))

(defn is-part-of-query
  "Returns a flattened list of parent, child, grandchild URIs."
	[url]
	(format "prefix dct: <http://purl.org/dc/terms/>
			select ?p ?gp ?ggp
			from <http://papyri.info/graph>
			where{ <%s> dct:isPartOf ?p .
				   ?p dct:isPartOf ?gp .
				   optional { ?gp dct:isPartOf ?ggp }
			}" url))

(defn batch-relation-query
  "Retrieves a set of triples where A `<dct:relation>` B when A is a child of the given URI."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a ?b
            from <http://papyri.info/graph>
            where { <%s> dct:hasPart ?a .
                    ?a dct:relation ?b
                    filter(!regex(str(?b),'/images$'))}" url))

(defn relation-query
  "Returns URIs that are the object of `<dct:relation>`s where the given URI is the subject."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a
            from <http://papyri.info/graph>
            where { <%s> dct:relation ?a
            filter(!regex(str(?a),'/images$'))}" url))

(defn primary-query
  "For HGV, APIS, or translations, finds the DDbDP or DCLP relation"
  [url]
  (format "prefix dct: <http://purl.org/dc/terms/>
           select ?a
           from <http://papyri.info/graph>
           where { <%s> dct:relation ?a
           filter(regex(str(?a),'/(ddbdp|dclp)/'))}" url))

(defn batch-replaces-query
  "Gets the set of triples where A `<dct:replaces>` B for a given collection."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a ?b
            from <http://papyri.info/graph>
            where { <%s> dct:hasPart ?a .
                    ?a dct:replaces ?b .
                    ?b dct:isReplacedBy ?a }" url))

(defn batch-hgv-source-query
  "Gets the set of triples where A `<dct:source` B for a given collection."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a ?b
              from <http://papyri.info/graph>
              where { <%s> dct:hasPart ?a .
                      ?a dct:source ?b }" url))

(defn hgv-source-query
  "Returns A where the given URI `<dct:source>` A."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a
              from <http://papyri.info/graph>
              where { <%s> dct:source ?a  }" url))

(defn batch-other-source-query
  "Gets `dct:source`s for items in a given collection where there are
  related HGV docs with sources."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a ?b
              from <http://papyri.info/graph>
              where { <%s> dct:hasPart ?a .
                    ?a dct:relation ?hgv .
                    ?hgv dct:source ?b }" url))

(defn other-source-query
  "Gets `dct:source`s for the given URI using related HGV docs."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a
              from <http://papyri.info/graph>
              where { <%s> dct:relation ?hgv .
                      ?hgv dct:source ?a }" url))


(defn batch-hgv-citation-query
  "Gets `dct:bibliographicCitation`s for items in a given collection where
  there are related sources with bibliography."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a ?c
              from <http://papyri.info/graph>
              where { <%s> dct:hasPart ?a .
                    ?a dct:source ?b .
                    ?b dct:bibliographicCitation ?c }" url))

(defn hgv-citation-query
  "Gets a bibliographic citation for the given URI, using the related source."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a
              from <http://papyri.info/graph>
              where { <%s> dct:source ?b .
                      ?b dct:bibliographicCitation ?a }" url))

(defn batch-other-citation-query
  "Gets bibliographic citations for items in a given collection, via their
  relationship with HGV records."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a ?c
              from <http://papyri.info/graph>
              where { <%s> dct:hasPart ?a .
                      ?a dct:relation ?hgv .
                      ?hgv dct:source ?b .
                      ?b dct:bibliographicCitation ?c }" url))

(defn other-citation-query
  "Gets bibliographic citations for a given item via its relationship
  to HGV records."
	[url]
    (format  "prefix dct: <http://purl.org/dc/terms/>
              select ?a
              from <http://papyri.info/graph>
              where { <%s> dct:relation ?hgv .
                      ?hgv dct:source ?b .
                      ?b dct:bibliographicCitation ?a }" url))

(defn batch-cited-by-query
  [url]
  (format "prefix cito: <http://purl.org/spar/cito/>
           prefix dct: <http://purl.org/dc/terms/>
           select ?a ?c
           from <http://papyri.info/graph>
           where {<%s> dct:hasPart ?a .
                  ?a dct:source ?b .
                  ?b cito:isCitedBy ?c }" url))

(defn cited-by-query
  "Looks for Cito citations coming from biblio"
  [url]
  (let [uri (.replace url "/source" "/work")]
    (format "prefix cito: <http://purl.org/spar/cito/>
             select ?a
             from <http://papyri.info/graph>
             where {<%s> cito:isCitedBy ?a }" uri)))

(defn replaces-query
  "Finds items that the given item replaces."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a
            from <http://papyri.info/graph>
            where { <%s> dct:replaces ?a }" url))

(defn batch-is-replaced-by-query
  "Finds items in a given collection that are replaced by other items."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a ?b
            from <http://papyri.info/graph>
            where { <%s> dct:hasPart ?a .
                    ?a dct:isReplacedBy ?b }" url))

(defn is-replaced-by-query
  "Finds any item that replaces the given item."
  [url]
  (format  "prefix dct: <http://purl.org/dc/terms/>
            select ?a
            from <http://papyri.info/graph>
            where { <%s> dct:isReplacedBy ?a }" url))

(defn batch-images-query
  [url]
  (format "prefix dct: <http://purl.org/dc/terms/>
           prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
           prefix olo: <http://purl.org/ontology/olo/core#>
           select ?a ?image
           from <http://papyri.info/graph>
           where { <%s> dct:hasPart ?a .
                   ?a dct:relation ?i .
                   ?i rdf:type olo:OrderedList .
                   ?i olo:slot ?slot .
                   ?slot olo:index ?index .
                   ?slot olo:item ?image }
           order by ?a ?index" url))

(defn images-query
  "Finds images related to the given url"
  [url]
  (let [uri (.replace url "/source" "/images")]
    (format "prefix olo: <http://purl.org/ontology/olo/core#>
             select ?image
             from <http://papyri.info/graph>
             where { <%1$s> olo:slot ?slot .
                      ?slot olo:item ?image .
                      ?slot olo:index ?i }
                      order by ?i" uri )))

;; ## Jena functions

(defn collect-row
  "Builds a row of results from Jena into a vector."
  [row rvars]
  (let [*row* (transient [])]
    (doseq [svar rvars]
      (when (not (nil? (.get row svar))) (conj! *row* (.toString (.get row svar)))))
    (persistent! *row*)))

(defn execute-query
  "Executes the query provided and returns a vector of vectors containing the results"
  [query]
  (try
    (with-open [exec (QueryExecutionHTTP/service (str server "/query") query)]
      (let [answer (.execSelect exec)]
        (loop [result []]
          (if (.hasNext answer)
            (recur (conj result
                         (collect-row (.next answer) (.getResultVars answer))))
            result))))
    (catch Exception e
      (println (.getMessage e))
      (println query))))

;; ## Data queueing functions

(defn queue-item
  "Adds the given URL to the @html queue for processing, along with associated data."
  [url]
  (let [relations (execute-query (relation-query url))
        primary (if (not (empty? (re-seq #"/(apis|hgv|hgvtrans)/" url)))
                  (execute-query (primary-query url))
                  '())
        replaces (execute-query (replaces-query url))
        is-replaced-by (execute-query (is-replaced-by-query url))
        is-part-of (execute-query (is-part-of-query url))
        source (if (empty? (re-seq #"/hgv/" url))
       		   	  (execute-query (other-source-query url))
       		   	  (execute-query (hgv-source-query url)))
        citation (if (empty? (re-seq #"/hgv" url))
       			  (execute-query (other-citation-query url))
       			  (execute-query (hgv-citation-query url)))
        biblio (execute-query (cited-by-query url))
        images (flatten (conj '() (execute-query (images-query url))
                     (filter
                       (fn [x] (> (count x) 0))
                       (for [r relations] (execute-query (images-query (first r)))))))
       ]
    (if (not (first primary))
      ;; If doc is being replaced, don't publish it, but make sure to publish its replacement.
      ;; This might be redundant, but we can't be sure.
      (if (not (first is-replaced-by))
        (.add @html (list (str "file:" (get-filename url))
                          (list "collection" (substring-before (substring-after url "http://papyri.info/") "/"))
                          (list "related" (apply str (interpose " " (for [x relations] (first x)))))
                          (list "replaces" (apply str (interpose " " (for [x replaces] (first x)))))
                          (list "isPartOf" (apply str (interpose " " (first is-part-of))))
                          (list "sources" (apply str (interpose " " (for [x source](first x)))))
                          (list "images" (apply str (interpose " " images)))
                          (list "citationForm" (apply str (interpose " " (for [x citation](first x)))))
                          (list "biblio" (apply str (interpose " " (for [x biblio] (first x)))))
                          (list "selfUrl" (substring-before url "/source"))
                          (list "server" nserver)))
        (queue-item (first (last is-replaced-by))))
      (queue-item (first (last primary))))))

(defn queue-items
  "Adds children of the given collection or volume to the @html queue for processing,
  along with a set of parameters."
  [url exclude prev-urls]
  (let [all-urls (cons url prev-urls)
        items (execute-query (has-part-query url))
        relations (execute-query (batch-relation-query url))
        replaces (execute-query (batch-replaces-query url))
        is-replaced-by (execute-query (batch-is-replaced-by-query url))
        all-sources (if (empty? (re-seq #"/hgv/" url))
                      (execute-query (batch-other-source-query url))
        			        (execute-query (batch-hgv-source-query url)))
        all-citations (if (empty? (re-seq #"/hgv/" url))
                        (execute-query (batch-other-citation-query url))
                        (execute-query (batch-hgv-citation-query url)))
        all-biblio (execute-query (batch-cited-by-query url))
        all-images (execute-query (batch-images-query url))]
    (doseq [item items]
      (let  [related (if (empty? relations) ()
                       (filter (fn [x] (= (first x) (last item))) relations))
             reprint-from (if (empty? replaces) ()
                            (filter (fn [x] (= (first x) (last item))) replaces))
             sources (if (empty? all-sources) ()
                       (filter (fn [x] (= (first x) (last item))) all-sources))
             citations (if (empty? all-citations) ()
                         (filter (fn [x] (= (first x) (last item))) all-citations))
             biblio (if (empty? all-biblio) ()
                      (filter (fn [x] (= (first x) (last item))) all-biblio))
             reprint-in (if (empty? is-replaced-by) ()
                          (filter (fn [x] (= (first x) (last item))) is-replaced-by))
             exclusion (some (set (for [x (filter
                                            (fn [s] (and (.startsWith (last s) "http://papyri.info")
                                                         (not (.contains (.toString (last s)) "/images/"))))
                                            related)]
                                    (substring-before (substring-after (last x) "http://papyri.info/") "/")))
                             exclude)
             images (if (empty? all-images) () (filter (fn [x] (= (first x) (last item))) all-images))
            ]
        (if (nil? exclusion)
          ( .add @html (list (str "file:" (get-filename (last item)))
                             (list "collection" (substring-before (substring-after (last item) "http://papyri.info/") "/"))
                             (list "related" (apply str (interpose " " (for [x related] (last x)))))
                             (list "replaces" (apply str (interpose " " (for [x reprint-from] (last x)))))
                             (list "isPartOf" (apply str (interpose " " all-urls)))
                             (list "sources" (apply str (interpose " " (for [x sources] (last x)))))
                             (list "images" (apply str (interpose " " (for [x images] (last x)))))
                             (list "citationForm" (apply str (interpose ", " (for [x citations] (last x)))))
                             (list "biblio" (apply str (interpose " " (for [x biblio] (last x)))))
                             (list "selfUrl" (substring-before (last item) "/source"))
                             (list "server" nserver)))
          (do (.add @links (list (get-html-filename
                                   (.toString
                                     (last
                                       (reduce (fn [x y]
                                                 (if (.contains (last x) exclusion) x y))
                                               related))))
                                 (get-html-filename (.toString (last item)))))
            (.add @links (list (get-txt-filename
                                 (.toString
                                   (last
                                     (reduce (fn [x y]
                                               (if (.contains (last x) exclusion) x y))
                                             related))))
                               (get-txt-filename (last item))))))))))

(defn queue-collections
  "Adds URLs to the HTML transform and indexing queues for processing.  Takes a URL, like
  `http://papyri.info/ddbdp`, a set of collections to exclude and recurses down to the item level."
  [url exclude prev-urls]
  ;; TODO: generate symlinks for relations
  ;; queue for HTML generation
   (let [all-urls (cons url prev-urls)
         items (execute-query (has-part-query url))]
    (when (> (count items) 0)
      (if (.endsWith (last (first items)) "/source")
        (queue-items url exclude prev-urls)
        (doseq [item items]
          (queue-collections (last item) exclude all-urls))))))

;; ## File generation and indexing functions

(defn delete-html
 "Get rid of any old/stale versions of related files"
 [files]
 (when (> (count files) 0)
   (doseq [file (.split files "\\s")]
     (let [fname (get-html-filename file)]
     (when-not (nil? fname)
       (let [f (File. fname)]
         (when (.exists f)
           (.delete f))))))))

(defn delete-text
  "Get rid of any old/stale versions of related files"
  [files]
  (when (> (count files) 0)
    (doseq [file (.split files "\\s")]
      (let [fname (get-txt-filename file)]
      (when-not (nil? fname)
        (let [f (File. fname)]
          (when (.exists f)
            (.delete f))))))))

(def re-start-end-quotes #"(^\"|\"$)")
(def re-delimiter #"\",\"")

(defn unwrap-and-escape
  [line]
  (if-not (st/blank? line)
    (map
      (fn[field]
        (st/replace
          (st/replace
            (st/replace
              (st/replace
                (st/replace
                  (st/replace field re-start-end-quotes, "")
                  "\\" "\\\\")
                "\"" "\\\"")
              "&" "&amp;")
            "<" "&lt;")
          #"\x0B" ""))
      (st/split line re-delimiter))
    '()))

(defn write-tm-xml
  [out name, fields, fieldnums, fns]
  (.write out (str "<" name ">\n"))
  (doseq [field fieldnums]
    (.write out (str "  <field n=\"" field "\">" (nth fields field) "</field>\n")))
    (doseq [f fns]
      (f))
  (.write out (str "</" name ">\n")))

(defn tm-file
  [n, name]
  (File. (str tmpath "/dump/files/" (int (Math/floor (/ (Integer. n) 1000))) "/" n "-" name ".clj")))


(defn process-tm-file
  [f, n]
  (with-open [rdr (io/reader (str tmpath "/dump/" f ".csv"))]
    (let [lseq (line-seq rdr)
          outfile (ref nil)
          record (ref nil)]
      (doseq [line lseq]
        (let [fields (unwrap-and-escape line)]
          (when-not (st/blank? (nth fields n))
            (let [of (tm-file (st/trim (nth fields n)) f)]
              (when (and (not (nil? @record)) (not= @outfile of)) ;; if we're on a new record, flush the old one
                (with-open [out (io/writer @outfile)]
                    (binding [*out* out
                              *print-dup* true]
                      (prn @record)
                      (dosync (ref-set record nil)))))
              (if-not (.exists of)
                (do ;; we're almost always creating a new file, so don't bother with expensive transactional stuff
                  (.mkdirs (.getParentFile of))
                  (with-open [out (io/writer of)]
                    (binding [*out* out
                              *print-dup* true]
                      (if-not (nil? fields)
                        (prn (vector fields))))))
                (do ;; if the file exists, we might already have it loaded from the last row, so don't re-read it
                  (if-not (= @outfile of)
                    (dosync
                      (ref-set outfile of)
                      (ref-set record (let [r (with-open [f (PushbackReader. (FileReader. @outfile))] (read f))]
                                                (into [] (concat r (vector fields))))))
                    (dosync
                      (ref-set record (into [] (concat @record (vector fields))))))))))))
      (when-not (or (nil? @outfile) (nil? @record))
        (with-open [out (io/writer @outfile)]
          (binding [*out* out
                    *print-dup* true]
            (prn @record)))))))

(defn map-tm-functions
  ([out name, fieldnums, rels]
    (for [values rels]
      (partial write-tm-xml out name values fieldnums [])))
  ([out, name, fieldnums, rels, fns]
    (for [values rels]
      (partial write-tm-xml out name values fieldnums fns))))

(defn preprocess-tm
  "Converts TM data into XML for inclusion in the PN."
  []
  (process-tm-file "dates" 1)
  (process-tm-file "texref" 1)
  (process-tm-file "editref" 0)
  (process-tm-file "geotex" 1)
  (process-tm-file "georef" 5)
  (process-tm-file "ref" 7)
  (process-tm-file "archref" 6)
  (process-tm-file "collref" 2)
  (let [rdr (io/reader (str tmpath "/dump/tex.csv"))
        lseq (line-seq rdr)]
    (doseq [line lseq]
      (let [fields (unwrap-and-escape line)
            outfile (File. (str tmpath "/files/" (int (Math/floor (/ (Integer. (nth fields 0)) 1000))) "/" (nth fields 0) ".xml"))]
        (.mkdirs (.getParentFile outfile))
        (with-open [out (io/writer outfile)]
          (let [datefile (tm-file (nth fields 0) "dates")
                dates (when (.exists datefile) (with-open [f (PushbackReader. (FileReader. datefile))] (read f)))
                texreffile (tm-file (nth fields 0) "texref")
                texrefs (when (.exists texreffile) (with-open [f (PushbackReader. (FileReader. texreffile))] (read f)))
                editreffile (tm-file (nth (nth texrefs 0) 0) "editref") ;; needs to be changed to texref
                editrefs (when (.exists editreffile) (with-open [f (PushbackReader. (FileReader. editreffile))] (read f)))
                geotexfile (tm-file (nth fields 0) "geotex")
                geotex (when (.exists geotexfile) (with-open [f (PushbackReader. (FileReader. geotexfile))] (read f)))
                georeffile (tm-file (nth fields 0) "georef")
                georefs (when (.exists georeffile) (with-open [f (PushbackReader. (FileReader. georeffile))] (read f)))
                personreffile (tm-file (nth fields 0) "ref")
                personrefs (when (.exists personreffile) (with-open [f (PushbackReader. (FileReader. personreffile))] (read f)))
                archreffile (tm-file (nth fields 0) "archref")
                archrefs (when (.exists archreffile) (with-open [f (PushbackReader. (FileReader. archreffile))] (read f)))
                collreffile (tm-file (nth fields 0) "collref")
                collrefs (when (.exists collreffile) (with-open [f (PushbackReader. (FileReader. collreffile))] (read f)))
                fns (concat
                      (map-tm-functions out "date" [2 3 4 9 14] dates)
                      (map-tm-functions out "texref" [1 2 3 4 5 14 15 19 21] texrefs
                        (map-tm-functions out "editref" [0 1] editrefs))
                      (map-tm-functions out "geotex" [2 20 22] geotex)
                      (map-tm-functions out "georef" [0 7 25 35 36 37 38] georefs)
                      (map-tm-functions out "personref" [0 132 53 54 55 56 151] personrefs)
                      (map-tm-functions out "archref" [5 37] archrefs)
                      (map-tm-functions out "collref" [1 14 15] collrefs)
                      )]
          (write-tm-xml out "text" fields [0 6 8 13 14 21 46 57 81 82 89] fns)))))))

(defn generate-html
  "Builds the HTML files for the PN."
  []
    (let [pool (Executors/newFixedThreadPool nthreads)
    tasks (map (fn [x]
         (fn []
           (try (.mkdirs (.getParentFile (File. (get-html-filename (first x)))))
            ;;(println "Transforming " (first x) " to " (get-html-filename (first x)))
            ;;(println x)
              (delete-html (last (nth x 2)))
              (delete-html (last (nth x 3)))
              (let [processor (Processor. false)
                    out (.newSerializer processor)])
              (transform (if (.startsWith (first x) "http")
                (str (.replace (first x) "papyri.info" nserver) "/rdf")
                (first x))
                (list (second x) (nth x 2) (nth x 3) (nth x 4) (nth x 5) (nth x 6) (nth x 7) (nth x 8) (nth x 9))
                (.newSerializer processor (FileOutputStream. (File. (get-html-filename (first x))))) @htmltemplates)
             (catch Exception e
               (.printStackTrace e)
               (println (str "Error converting file " (first x) " to " (get-html-filename (first x))))))))
       @html)]
    (doseq [^Future future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))
    (dosync (ref-set text @html)
      (ref-set htmltemplates nil)))

(defn generate-text
  "Builds the text files for the PN."
  []
    (let [pool (Executors/newFixedThreadPool nthreads)
        tasks (map (fn [x]
         (fn []
           (when (not (.startsWith (first x) "http"))
      (try (.mkdirs (.getParentFile (File. (get-html-filename (first x)))))
        (delete-text (last (nth x 2)))
        (delete-text (last (nth x 3)))
        (transform (if (.startsWith (first x) "http")
        							   ((println "File is " + (first x))
                                       (str (.replace (first x) "papyri.info" nserver) "/rdf"))
                                       (first x))
        (list (second x) (nth x 2) (nth x 3) (nth x 4))
        (.newSerializer processor (FileOutputStream. (File. (get-txt-filename (first x))))) @texttemplates)
        (catch Exception e
          (.printStackTrace e)
          (println (str "Error converting file " (first x) " to " (get-txt-filename (first x)))))))))
       @text)]
    (doseq [^Future future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))
    (dosync (ref-set texttemplates nil)))

(defn print-words
  "Dumps accumulated word lists into a file."
  []
     (let [out (FileWriter. (File. "/srv/data/papyri.info/words.txt"))]
       (for [word @words]
   (.write out (str word "\n")))))

(defn commit-and-optimize
  "Runs an asynchronous commit and then optimize on the named Solr index."
  [index]
  (let [solr (.build (.withSocketTimeout (HttpSolrClient$Builder. (str solrurl index "/")) 3600000))]
    (.commit solr false false )
    ;;(.optimize solr false false)
    (.close solr)))

(defn load-morphs
  "Loads morphological data from the given file into the morph-search Solr index."
 [file]
 (let [value (StringBuilder.)
       handler (proxy [DefaultHandler] []
		 (startElement [uri local qname atts]
			       (set! *current* qname)
			       (when (= qname "analysis")
				 (set! *doc* (SolrInputDocument. (make-array String 0)))
				 (set! *index* (+ *index* 1))
				 (.addField *doc* "id" *index*)))
		 (characters [ch start length]
			     (when-not (.startsWith *current* "analys")
			       (.append value ch start length)))
		 (endElement [uri local qname]
			     (if (not (.startsWith qname "analys"))
			       (.addField *doc* *current* (.toString value))
			       (.add @solr *doc*))
			     (set! *current* "analysis")
			     (.delete value 0 (.length value))))]
   (.. SAXParserFactory newInstance newSAXParser
       (parse (InputSource. (FileInputStream. file))
	      handler))))

;; ## Java static methods

(defn -loadLemmas
  "Calls the morphological data-loading procedure on the Greek and Latin XML files from Perseus.
  NOTE: hard code file paths."
  []
  (binding [*current* nil
      *doc* nil
	    *index* 0]
    (dosync (ref-set solr 
      (let [cb (ConcurrentUpdateSolrClient$Builder. (str solrurl "morph-search/"))]
        (-> cb (.withQueueSize 5000) 
          (.withThreadCount nthreads)
          (.build))))
	    (.setRequestWriter @solr (BinaryRequestWriter.)))
    (load-morphs "/srv/data/papyri.info/git/navigator/pn-lemmas/greek.morph.unicode.xml")
    (load-morphs "/srv/data/papyri.info/git/navigator/pn-lemmas/latin.morph.xml")
    (commit-and-optimize "morph-search")))

(defn -loadBiblio
  "Loads bibliographic data into the biblio-search Solr index."
  []
  (init-templates (str xsltpath "/Biblio2Solr.xsl") nthreads "info.papyri.indexer/bibsolrtemplates")
  (init-templates (str xsltpath "/Biblio2HTML.xsl") nthreads "info.papyri.indexer/bibhtmltemplates")
  (dosync (ref-set solrbiblio 
    (let [cb (ConcurrentUpdateSolrClient$Builder. (str solrurl "biblio-search/"))]
      (-> cb (.withQueueSize 1000) 
        (.withThreadCount nthreads)
        (.build))))
    (.setRequestWriter @solrbiblio (BinaryRequestWriter.)))

  ;; Generate and Index bibliography
  (println "Generating and indexing bibliography...")
  (let [pool (Executors/newFixedThreadPool nthreads)
        htmlpath (str htpath "/biblio/")
        files (file-seq (File. (str filepath "/Biblio")))
        tasks (map (fn [x]
                    (fn []
                      (transform (str "file://" (.getAbsolutePath x)) () (bibliodochandler) @bibsolrtemplates)
                      (let [out (File. (str htmlpath (.getName (.getParentFile x)) "/" (.replace (.getName x) ".xml" ".html")))]
                        (.mkdirs (.getParentFile out))  
                        (transform (str "file://" (.getAbsolutePath x)) ()
                                  (.newSerializer processor (FileOutputStream. out))
                                  @bibhtmltemplates))))
                    (filter #(.endsWith (.getName %) ".xml") files))]
    (doseq [^Future future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))

  (println "Optimizing index...")
  (commit-and-optimize "biblio-search"))

(defn queue-docs
  [args]
  (dosync (ref-set html (ConcurrentLinkedQueue.)))
  (if (nil? (first args))
    (do
      (println "Queueing DDbDP...")
      (queue-collections "http://papyri.info/ddbdp" () ())
      (println (str "Queued " (count @html) " documents."))
      (println "Queueing DCLP...")
      (queue-collections "http://papyri.info/dclp" '("ddbdp") ())
      (println (str "Queued " (count @html) " documents."))
      (println "Queueing HGV...")
      (queue-collections "http://papyri.info/hgv" '("ddbdp", "dclp") ())
      (println (str "Queued " (count @html) " documents."))
      (println "Queueing APIS...")
      (queue-collections "http://papyri.info/apis" '("ddbdp", "dclp", "hgv") ())
      (println (str "Queued " (count @html) " documents.")))
    (doseq [arg args] (queue-item arg))))

(defn generate-pages
  []
  (dosync (ref-set text @html))
  ;; Generate HTML
  (println "Generating HTML...")
  (generate-html)

  ;; Generate text
  (println "Generating text...")
  (generate-text))

(defn get-cached
  [file, args]
  (if (.exists (io/as-file file))
    (let [queue (ConcurrentLinkedQueue.)
          ois (ObjectInputStream. (io/input-stream file))]
      (.readObject queue ois)
      (dosync (ref-set html queue)))
    (queue-docs args)))

(defn cache
  [file]
  (let [oos (ObjectOutputStream. (io/output-stream file))]
    (.writeObject oos @html)))

(defn -generatePages
  "Builds the HTML and plain text pages for the PN"
  [args]
  (init-templates (str xsltpath "/MakeHTML.xsl") nthreads "info.papyri.indexer/htmltemplates")
  (init-templates (str xsltpath "/MakeSolr.xsl") nthreads "info.papyri.indexer/solrtemplates")
  (init-templates (str xsltpath "/MakeText.xsl") nthreads "info.papyri.indexer/texttemplates")
  (case (first args)
        "-serialize" (do (get-cached (second args) (rest (rest args)))
                         (generate-pages)
                         (cache (second args)))
        (do (queue-docs args)
            (generate-pages))))

(defn -index
  "Runs the main PN indexing process."
  []
  (dosync (ref-set solr
    (let [cb (ConcurrentUpdateSolrClient$Builder. (str solrurl "pn-search/"))]
      (-> cb (.withQueueSize 50) 
        (.withThreadCount 1)
        (.build))))
	  (.setRequestWriter @solr (BinaryRequestWriter.)))

  ;; Index docs queued in @text
  (println "Indexing text...")
  (let [pool (Executors/newFixedThreadPool 1)
        tasks
    	(map (fn [x]
  	       (fn []
  		 (when (not (.startsWith (first x) "http"))
  		   (transform (first x)
  			      (list (second x) (nth x 2) (nth x 6))
  			      (dochandler) @solrtemplates)))) @text)]
    (doseq [^Future future (.invokeAll pool tasks)]
      (.get future))
    (doto pool
      (.shutdown)))

  (println "Optimizing index...")
  (commit-and-optimize "pn-search")

  (dosync (ref-set html nil)
    (ref-set text nil)
    (ref-set solrtemplates nil))

  ;;(print-words)
  )


(defn -main [& args]
  (if (> (count args) 0)
    (case (first args)
      "load-lemmas" (-loadLemmas)
      "biblio" (-loadBiblio)
      "generate-pages" (-generatePages (rest args))
      "process-tm" (preprocess-tm)
      (do (-generatePages args)
        (-index)))
    (do (-generatePages args)
      (-index))))

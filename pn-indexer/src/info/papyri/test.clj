(ns info.papyri.indexer
  (:use clojure.contrib.math)
  (:import 
    (com.hp.hpl.jena.rdf.model Model ModelFactory Resource ResourceFactory)
    (java.io File FileInputStream FileReader StringWriter)
    (java.net URL URLEncoder)
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
    (org.xml.sax InputSource)
    (org.xml.sax.helpers DefaultHandler)
    ))
    
(def html (ref (ConcurrentLinkedQueue.)))

(defn load-relations
  [url]
  (let [relations (.read (ModelFactory/createDefaultModel (relation-query url) "RDF/XML"))
        replaces (.read (ModelFactory/createDefaultModel (replaces-query url) "RDF/XML"))
        is-replaced-by (.read (ModelFactory/createDefaultModel (is-replaced-by-query url) "RDF/XML"))
        subjects (.listResourcesWithProperty relations (ResourceFactory/createProperty "http://purl.org/dc/terms/relation"))] 
      (while (.hasNext subjects)
        (let [item (.next subjects)
              reprint-from (.listObjectsOfProperty replaces item (ResourcesFactory/createProperty "http://purl.org/dc/terms/replaces"))
              reprint-in (.listObjectsOfProperty replaces item (ResourcesFactory/createProperty "http://purl.org/dc/terms/isReplacedBy"))]
              (.add @html '((get-filename (.toString item)) 
                (apply str (interpose " " (for [x (iterator-seq reprint-from)] (.toString x)))) 
                (apply str (interpose " " (for [x (iterator-seq reprint-in)] (.toString x)))))))))))
                
(def html (ref (ConcurrentLinkedQueue.)))
(queue-items "http://papyri.info/hgv/BGU_1" '("ddbdp"))
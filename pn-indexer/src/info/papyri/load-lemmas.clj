(ns info.papyri.lemmas
  (:use clojure.contrib.math)
  (:import
    (clojure.lang ISeq)
    (com.hp.hpl.jena.rdf.model Model ModelFactory Resource ResourceFactory)
    (java.io File FileInputStream FileOutputStream FileReader StringWriter)
    (java.net URI URL URLEncoder URLDecoder)
    (java.util ArrayList TreeMap)
    (java.util.concurrent Executors ConcurrentLinkedQueue)
    (javax.xml.parsers SAXParserFactory)
    (javax.xml.transform Result )
    (javax.xml.transform.sax SAXResult)
    (javax.xml.transform.stream StreamSource StreamResult)
    (net.sf.saxon Configuration FeatureKeys PreparedStylesheet StandardErrorListener StandardURIResolver TransformerFactoryImpl)
    (net.sf.saxon.trans CompilerInfo XPathException)
    (org.apache.solr.client.solrj SolrServer)
    (org.apache.solr.client.solrj.impl CommonsHttpSolrServer StreamingUpdateSolrServer BinaryRequestWriter)
    (org.apache.solr.client.solrj.request RequestWriter)
    (org.apache.solr.common SolrInputDocument)
    (org.xml.sax InputSource)
    (org.xml.sax.helpers DefaultHandler)))

(def solrurl "http://localhost:8082/solr/morph-search/")
(def documents (ref (ConcurrentLinkedQueue.)))
(def *current*)
(def *doc*)
(def *index*)

(defn index-solr
  []
  (.start (Thread. 
	   (fn []
	     (let [solr (StreamingUpdateSolrServer. solrurl 5000 5)]
	       (.setRequestWriter solr (BinaryRequestWriter.))
	       (while (= (count @documents) 0)
		 (Thread/sleep 2000))
	       (when (> (count @documents) 0)
		 (let [docs (ArrayList.)]
		   (.addAll docs @documents)
		   (.removeAll @documents docs)
		   (.add solr docs)))
	       )
	     (Thread/sleep 10000)
	     (when (> (count @documents) 0)
	       (index-solr))))))

(defn load-morphs 
  [file]
  (let [value (StringBuilder.)
	handler (proxy [DefaultHandler] []
          (startElement [uri local qname atts]
			(set! *current* qname)
			(when (= qname "analysis")
			  (set! *doc* (SolrInputDocument.))
			  (set! *index* (+ *index* 1))
			  (.addField *doc* "id" *index*)))
          (characters [ch start length]
		      (when-not (.startsWith *current* "analys")
			(.append value ch start length)))
	  (endElement [uri local qname]
		      (if (not (.startsWith qname "analys"))
			(.addField *doc* *current* (.toString value))
			(do
			  (dosync (.add @documents *doc*))
			  (when (> (count @documents) 5000)
			    (index-solr))))
		      (set! *current* "analysis")
		      (.delete value 0 (.length value)))
	  (endDocument []
		       (index-solr)))]
    (.. SAXParserFactory newInstance newSAXParser
                    (parse (InputSource. (FileInputStream. file)) 
			   handler))))

(defn -main [& args]
  (binding [*current* nil
	    *doc* nil
	    *index* 0]
    (index-solr)
    (load-morphs "/data/papyri.info/svn/pn/pn-lemmas/greek.morph.unicode.xml")
    (load-morphs "/data/papyri.info/svn/pn/pn-lemmas/latin.morph.xml")
    (let [solr (CommonsHttpSolrServer. solrurl)]
      (.commit solr))
    ))

(-main)
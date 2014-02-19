Overview
========
The applications in this directory all concern storage of and access to papyri.info records; that is to say, their purpose is persistence, retrieval, and display rather than editing or creation of new texts, and they thus relate chiefly to the papyri.info navigator rather than the editor.

In broad outline, the packages below have the following tasks:

1. **pn-dispatcher** - Java servlets for persistence, retrieval, and display of  
   papyri.info records once processed by pn-indexer, pn-mapping, pn-lemmas, pn-solr, and pn-xslt files. Three servlets are contained here:
  1. `info.papyri.dispatch.browse.CollectionBrowser` - for browsing by collection name
  2. `info.papyri.dispatch.browse.facet.FacetBrowser` - for faceted browse and search using a variety of criteria
  3. `info.papyri.dispatch.atom` - outputs an atom feed summarising texts, limitable by edit date
2. **pn-indexer** - Collates XML files using information from the Numbers Server  
  and feeds information drawn from them into the Solr index
3. **pn-lemmas** - Adds lemmatisation information to the Solr index
4. **pn-mapping** - parses the XML files in order to resolve identifiers and retrieve collation and collection information; stores the result as triples in the Numbers Server.
5. **pn-site** - HTML, JS, CSS, and img files for the Navigator site
6. **pn-solr** - config and other required files for the Solr index
7. **pn-sync** - synchronises the contents of the numbers server and Solr index with the contents of the 'mothership' repository (i.e., publishes texts entered via the editor to the navigator)
8. **pn-xslt** - XSLT stylesheets used for HTML display and Solr indexing.

Environment
===========
As the above implies, three components are required for the Navigator to function.

1. A servlet container, such as Tomcat, for the Java servlets
2. A Numbers Server, for storing RDF information correlating the various files
3. A Solr server, for search functionality.

A sample script for appropriately starting/stopping these three components - dlib_tomcat_pi - is provided. This script should give you most of the information you need. 

Note that in this script, stoptc and starttc both refer to stopping/starting the editor. To stop and start the navigator and associated services, use startsolr and stopsolr.

Certain apache settings - in particular rewrite rules - are fundamental to the correct application function. These are given in the pi.conf file.

The Servlet container
---------------------

The servlet container used is Tomcat 7.0.16, with the servlets being deployed in the webapps directory as a .war file (dispatch.war).

Note the additional memory allocation here, with JAVA_OPTS settings at -Xms768m -Xmx768m -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSClassUnloadingEnabled. This is largely necessitated by the performance demands of the Solr indexing process.

Even more importantly, note that the location of the solr.solr.home variable is set in JAVA_OPTS (normally solr.solr.home=/data/papyri.info/solr). This location is not the default location, and without explicit setting of the solr home, servlets will be unable to locate the Solr index.

The pi.conf file assumes that Tomcat will be running on port 8083.

The Numbers Server
------------------

The numbers server triple store is Jena-Fuseki 0.2.2. It is assumed to be available on port 8090.

The Solr Server
---------------

As noted above, Solr home is assumed to be located at /srv/data/papyri.info/solr. With the exception of the data directory, this directory should contain the files and structure given in pn-solr, or simply symlinks to these.

Because Solr runs in the same webapps directory as dispatch.war, it will also be available over port 8083.

Note that the Solr deployment is multicore, with cores pn-search and pn-search-offline. Indexing is done into pn-search-offline, which is then swapped for the pn-search core to bring the newly created index online.


Directory Structure
===================

The directory structure is expected to conform to:

/srv/data/papyri.info/solr - for Solr home  
/srv/data/papyri.info/jena - for numbers server data storage  
/srv/data/papyri.info/git/navigator - holding the gthub-synchronised application files themselves  
/srv/data/papyri.info/idp.data - IDP XML files (the 'mothership' repository), as given at <https://github.com/papyri/idp.data>  
/srv/data/papyri.info/pn/idp.html - the IDP HTML files generated from the files in /data/papyri.info/idp.data  
/srv/data.papyri.info/pn/home - the HTML, CSS, JS, and image files used throughout the site and in interactive interfaces. This directory can consist simply of a symlink to /srv/data/papyri.info/git/navigator/pn-site.  

Adapting to a different directory structure is straightforward, but will mean finding where there are hard-coded directory locations and changing them.

Building and Running
====================

Java servlets are built using Maven 3.0.3. Once Maven is installed, dependency calculation and download should proceed automatically - though note in particular the existence of the project's Maven repo at <http://dev.papyri.info/maven/>.

Applications written in Clojure (pn-mapping, pn-indexer, and pn-lemmas) will require [leiningen](https://github.com/technomancy/leiningen "leiningen on github") - which is itself Maven-based.



#!/bin/bash
# Make destination directories
DATE=`date --rfc-3339=date`
cd /data/papyri.info/packages
mkdir $DATE
mkdir $DATE/git
mkdir $DATE/xml
mkdir $DATE/html
mkdir $DATE/text
mkdir $DATE/rdf

git clone --bare /data/papyri.info/idp.data "$DATE/git"
rsync -a --exclude ".git" /data/papyri.info/idp.data/ "$DATE/xml"
rsync -a --include "*.html" --exclude "*.txt" /data/papyri.info/pn/idp.html/ "$DATE/html"
rsync -a /data/papyri.info/pn/home/css "$DATE/html/"
rsync -a /data/papyri.info/pn/home/images "$DATE/html/"
rsync -a /data/papyri.info/pn/home/js "$DATE/html/"
rsync -a --include "*.txt" --exclude "*.html" /data/papyri.info/pn/idp.html/ "$DATE/text"
rsync -a /data/papyri.info/pn/home "$DATA/html/"
QUERY="CONSTRUCT%20%7B%20%3Fs%20%3Fp%20%3Fo%20%7D%20FROM%20%3Chttp%3A%2F%2Fpapyri.info%2Fgraph%3E%20WHERE%20%7B%20%3Fs%20%3Fp%20%3Fo%20%7D"
curl -H "Accept: text/plain" -H "Accept-Charset: utf-8" "http://localhost/sparql?query=$QUERY&output=n3" > "$DATE/rdf/papyri.info.nt"

rm -rf *.zip
zip -rq "$DATE.zip" $DATE
#rm -rf $DATE

#!/bin/bash
# Make destination directories
DATE=`date --rfc-3339=date`
cd /data/papyri.info/packages
mkdir $DATE
mkdir $DATE/xml
mkdir $DATE/html
mkdir $DATE/text
mkdir $DATE/rdf

git clone /data/papyri.info/idp.data "$DATE/xml"
rsync -v --include "*.html" --exclude "*.txt" /data/papyri.info/pn/idp.html/ "$DATE/html"
rsync -v --include "*.txt" --exclude "*.html" /data/papyri.info/pn/idp.html/ "$DATE/text"
rsync -a /data/papyri.info/pn/home "$DATA/html/"
QUERY="CONSTRUCT%20%7B%20%3Fs%20%3Fp%20%3Fo%20%7D%0AFROM%20%3Crmi%3A%2F%2Flocalhost%2Fpapyri.info%23pi%3E%0AWHERE%20%7B%20%3Fs%20%3Fp%20%3Fo%20%7D"
curl -H "Accept: text/plain" -H "Accept-Charset: utf-8" "http://localhost/sparql/?query=$QUERY&output=n3" > "$DATE/rdf/papyri.info.n3"

rm -rf *.zip
zip -r "$DATE.zip" $DATE
rm -rf $DATE

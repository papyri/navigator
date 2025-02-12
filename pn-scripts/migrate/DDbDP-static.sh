#!/bin/bash

if [ ! -d "DDB_EpiDoc_XML" ]; then
    echo "This script must be run from the root of the idp.data repository"
    exit 1
fi

if [ ! -d "Published" ]; then
    mkdir "Published"
fi

touch errors.log
HOME=`pwd`
cd "DDB_EpiDoc_XML"
for d in `find . -type d`; do
    cd "$d"
    if [ $(ls *.xml 2> /dev/null | wc -l) -gt 0 ]; then
        echo "Processing $d"
        for f in `ls *.xml`; do
            NAME=`grep -e "<idno type=\"ddb-hybrid\">[^<]*</idno>" -o $f | sed 's/<idno type="ddb-hybrid">//' | sed 's/<\/idno>//' | sed -E 's/;+/\//g'`.xml
            DIR=`dirname $NAME`
            if [ ! -d "$HOME/Published/#DIR" ]; then
                mkdir -p "$HOME/Published/$DIR"
            fi
            if [ ! -f "$HOME/Published/$NAME" ]; then
                git mv $f "$HOME/Published/$NAME"
            else
                echo "File $HOME/Published/$NAME already exists" >> "$HOME/errors.log"
            fi
        done
    fi
    cd "$HOME/DDB_EpiDoc_XML"
done
cd "$HOME"
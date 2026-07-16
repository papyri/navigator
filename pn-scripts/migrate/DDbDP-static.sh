#!/bin/bash

if [ ! -d "DDB_EpiDoc_XML" ]; then
    echo "This script must be run from the root of the idp.data repository"
    exit 1
fi

if [ ! -d "Historical" ]; then
    mkdir "Historical"
fi

touch errors.log
HOME=`pwd`
cd "DDB_EpiDoc_XML"
for d in `find . -type d`; do
    cd "$d"
    if [ $(ls *.xml 2> /dev/null | wc -l) -gt 0 ]; then
        echo "Processing $d"
        for f in `ls *.xml`; do
            NAME=`grep -e "<idno type=\"ddb-hybrid\">[^<]*</idno>" -o $f | sed 's/<idno type="ddb-hybrid">//' | sed 's/<\/idno>//'`
            NAME=`echo $NAME | sed 's/\//_/g' | sed -E 's/;+/\//g' | sed 's/,/-/g'`.xml
            DIR=`dirname $NAME`
            if [ ! -d "$HOME/Historical/#DIR" ]; then
                mkdir -p "$HOME/Historical/$DIR"
            fi
            if [ ! -f "$HOME/Historical/$NAME" ]; then
                git mv $f "$HOME/Historical/$NAME"
                
            else
                echo "File $HOME/Historical/$NAME already exists" >> "$HOME/errors.log"
            fi
        done
    fi
    cd "$HOME/DDB_EpiDoc_XML"
done
cd "$HOME"
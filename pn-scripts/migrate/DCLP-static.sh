#!/bin/bash

if [ ! -d "DCLP" ]; then
    echo "This script must be run from the root of the idp.data repository"
    exit 1
fi

if [ ! -d "Historical" ]; then
    mkdir "Historical"
fi

touch errors.log
HOME=`pwd`
cd "DCLP"
for d in `find . -type d`; do
    cd "$d"
    if [ $(ls *.xml 2> /dev/null | wc -l) -gt 0 ]; then
        echo "Processing $d"
        for f in `ls *.xml`; do
          for DCLP in `grep -e "<idno type=\"dclp-hybrid\">[^<]*</idno>" -o $f | sed 's/<idno type="dclp-hybrid">//' | sed 's/<\/idno>//'`; do 
            NAME=`echo $DCLP | sed 's/\//_/g' | sed -E 's/;+/\//g' | sed 's/,/-/g'`.xml
            if [[ "$NAME" != tm/* ]]; then
              DIR=`dirname $NAME`
              if [ ! -d "$HOME/Historical/$DIR" ]; then
                  mkdir -p "$HOME/Historical/$DIR"
              fi
              if [ ! -f "$HOME/Historical/$NAME" ]; then
                  saxon -s:$f -xsl:$HOME/../navigator/pn-scripts/migrate/update-DCLP-static.xsl -o:"$HOME/Historical/$NAME" filename="$DCLP"
              else
                  echo "File $HOME/Historical/$NAME already exists" >> "$HOME/errors.log"
              fi
            fi
          done
        done
    fi
    cd "$HOME/DCLP"
done
cd "$HOME"
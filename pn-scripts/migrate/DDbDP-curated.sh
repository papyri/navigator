#!/bin/bash

if [ ! -d "DDB_EpiDoc_XML" ]; then
    echo "This script must be run from the root of the idp.data repository"
    exit 1
fi

if [ ! -d "DDbDP" ]; then
    mkdir "DDbDP"
fi

touch errors.log
HOME=`pwd`
cd "DDB_EpiDoc_XML"
for d in `find . -type d`; do
    cd "$d"
    if [ $(ls *.xml 2> /dev/null | wc -l) -gt 0 ]; then
        echo "Processing $d"
        for f in `grep -rl "<lb" .`; do
            REPRINT=`grep -e "reprint-in" -o -m 1 $f`
            if [ -z "$REPRINT" ]; then
                TM=`grep -e "<idno type=\"TM\">[^<]*</idno>" -o -m 1 $f | sed 's/<idno type="TM">//' | sed 's/<\/idno>//'`
                HGV=`grep -e "<idno type=\"HGV\">[^<]*</idno>" -o -m 1 $f | sed 's/<idno type="HGV">//' | sed 's/<\/idno>//'`
                # If there are multiple TM numbers, prefer the first one
                if [[ "$TM" == *" "* ]]; then
                    ID=`echo $TM | sed -E 's/^([^ ]*) .*/\1/'`
                # If there are multiple HGV numbers, prefer the TM one
                elif [[ "$HGV" == *" "* ]]; then
                    ID=$TM
                else
                    ID=$HGV
                fi
                if [ ! -z "$ID" ]; then
                    STRIPPED_ID=`echo $ID | sed 's/[^0-9]//g'`
                    if [ ${#STRIPPED_ID} -lt 4 ]; then
                        FOLDER="0"
                    else
                        FOLDER=`echo $STRIPPED_ID | sed 's/...$//'`
                    fi
                    if [ ! -d "$HOME/DDbDP/$FOLDER" ]; then
                        echo "Creating folder $FOLDER"
                        mkdir "$HOME/DDbDP/$FOLDER"
                    fi
                    if [ ! -f "$HOME/DDbDP/$FOLDER/$ID.xml" ]; then
                        git mv $f "$HOME/DDbDP/$FOLDER/$ID.xml"
                    else
                        echo "File $ID.xml already exists" >> "$HOME/errors.log"
                    fi
                else 
                    echo "No TM ID found in $f" >> "$HOME/errors.log"
                fi
            fi
        done
    fi
    cd "$HOME/DDB_EpiDoc_XML"
done
cd "$HOME"
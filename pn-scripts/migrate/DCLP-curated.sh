#!/bin/bash

if [ ! -d "DCLP" ]; then
    echo "This script must be run from the root of the idp.data repository"
    exit 1
fi

HOME=`pwd`
cd "DCLP"
for d in `find . -type d`; do
    cd "$d"
    if [ $(ls *.xml 2> /dev/null | wc -l) -gt 0 ]; then
        echo "Processing $d"
        for f in `ls *.xml`; do
            ID=`echo $f | sed 's/.xml//'`
            if [ ${#ID} -lt 4 ]; then
                FOLDER="0"
            else
                FOLDER=`echo $ID | sed 's/[^0-9]*//' | sed 's/...$//'`
            fi
            if [ ! -d "$HOME/DCLP/$FOLDER" ]; then
                echo "Creating folder $FOLDER"
                mkdir "$HOME/DCLP/$FOLDER"
            fi
            if [ ! -f "$HOME/DCLP/$FOLDER/$ID.xml" ]; then
                git mv $f "$HOME/DCLP/$FOLDER/$ID.xml"
            fi
        done
    fi
    cd "$HOME/DCLP"
done
cd "$HOME"
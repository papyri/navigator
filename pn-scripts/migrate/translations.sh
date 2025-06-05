#!/bin/bash

if [ ! -d "HGV_trans_EpiDoc" ]; then
    echo "This script must be run from the root of the idp.data repository"
    exit 1
fi

mkdir -p "Translations"

HOME=`pwd`
cd "HGV_trans_EpiDoc"
git mv glossary.xml "$HOME/Translations/glossary.xml"
for f in `ls *.xml`; do
    ID=`echo $f | sed 's/.xml//'`
    if [ ${#ID} -lt 4 ]; then
        FOLDER="0"
    else
        FOLDER=`echo $ID | sed 's/[^0-9]*//' | sed 's/...$//'`
    fi
    if [ ! -d "$HOME/Translations/$FOLDER" ]; then
        echo "Creating folder $FOLDER"
        mkdir "$HOME/Translations/$FOLDER"
    fi
    if [ ! -f "$HOME/Translations/$FOLDER/$ID-1.xml" ]; then
        git mv $f "$HOME/Translations/$FOLDER/$ID-1.xml"
        saxon -s:"$HOME/Translations/$FOLDER/$ID-1.xml" -xsl:"$HOME/../navigator/pn-scripts/migrate/translations.xsl" -o:"$HOME/Translations/$FOLDER/$ID.xml"
    fi
done
cd "$HOME"
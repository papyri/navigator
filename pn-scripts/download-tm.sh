#!/bin/bash

if [ ! -d "/srv/data/papyri.info/TM" ]; then
    mkdir -p "/srv/data/papyri.info/TM"
fi
cd "/srv/data/papyri.info/TM"
# Build TM number list
rm -f tm_numbers.txt
rm -f download-failures.log
# Get all TM numbers from DDbDP and DCLP
ls -R $1/DDbDP | grep ".xml" | sed 's/[^0-9]*//g' >> tm_numbers.txt
sort -un tm_numbers.txt -o tm_numbers.txt
for f in `grep -Rl -E "<idno type=\"TM\">\S+\s" $1/DDbDP`; do
    FILE=`basename $f`
    TMNUM=`echo $FILE | sed 's/[^0-9]*//g'`
    if [ ${#TMNUM} -lt 4 ]; then
        FOLDER="0"
    else
        FOLDER=`echo $TMNUM | sed 's/...$//'`
    fi
    TMS=`grep  "<idno type=\"TM\">" $f | sed 's/ *[^>]*>\([ 0-9]*\)<.*/\1/'`
    declare -a TMARRAY=(`echo $TMS | sed 's/ / /g'`)
    for tm in "${TMARRAY[@]}"; do
        echo $tm >> tm_numbers.txt
    done
done
ls -R $1/DCLP | grep ".xml" | sed 's/[^0-9]*//g' >> tm_numbers.txt
if [ -f replacements.txt ]; then
    cat replacements.txt >> tm_numbers.txt
fi
sort -un tm_numbers.txt -o tm_numbers.txt

# Build exclude list (files modified in last 2 weeks)
find . -name "*.json" -ctime -14 | sed 's/.*\/\([0-9]*\).json$/\1/g' | sort -un > exclude.txt
sort -un exclude.txt -o exclude.txt
# Get the list of TM numbers that haven't been updated in 2 weeks
comm -23 tm_numbers.txt exclude.txt > process_tm.txt
for tm in `cat process_tm.txt`; do
    if [ ${#tm} -lt 4 ]; then
        FOLDER="0"
    else
        FOLDER=`echo $tm | sed 's/...$//'`
    fi
    if [ ! -d "$FOLDER" ]; then
        mkdir "$FOLDER"
    fi
    if [ ! -f "$FOLDER/$tm.json" ]; then
        # Error conditions: curl might fail, or the returned file might contain "error"
        # The TM API does not yet return proper HTTP error codes, so we have to check the content
        curl -sS -H "user: $2" -H "key: $3" "https://www.trismegistos.org/dataservices/API/ddbdp_api/endpoint.php?source=ddbdp&id=$tm" -o "$FOLDER/$tm.json"
        if [ $? -ne 0 ]; then
            echo "$tm" >> download-failures.log
            grep -q "error" "$FOLDER/$tm.json"
            if [ $? -eq 0 ]; then
                echo "$tm" >> download-failures.log
                rm -f "$FOLDER/$tm.json"
            fi
        fi
    fi
    sleep .5s
done
if [ -f download-failures.log ]; then
    FAILURES=`cat download-failures.log | wc -l | tr -d ' \n'`
    echo "$FAILURES downloads failed. See download-failures.log for details."
fi

#!/bin/bash

# script for updating existdb and dclp-corpusoverview. needs to be executable

while getopts ":cdn:o:" opt; do
	case ${opt} in
		c)	complete=set
			;;
		d)	debug=set
			;;
		n)	git_commit_new=$OPTARG
			;;
		o)	git_commit_old=$OPTARG
			;;
		?)	echo "Invalid option: -$OPTARG" >&2 && exit 1
			;;
	esac
done

git_repository="/srv/data/papyri.info/idp.data"
corpora="APIS Biblio DCLP DDB_EpiDoc_XML HGV_meta_EpiDoc HGV_trans_EpiDoc"
server="localhost"

if [ ! -n "${complete}" ]
	then	git_diff=$(cd ${git_repository} && git diff --name-only ${git_commit_old} ${git_commit_new})
fi

for corpus in ${corpora}
do	if [ -n "${complete}" ]
		then	corpus_tm=$(ls -lR ${git_repository}/${corpus} | awk '{ print $9 } ' | sed '/^$/d' | rev | awk '{ print substr($0,5) }' | rev)
		else	corpus_tm=$(echo "${git_diff}" | grep ${corpus} | rev | awk -F  "/" '{print $1}' | awk '{ print substr($0,5) }' | rev)
	fi

	if [ -n "${corpus_tm}" ]
		then	# for batch the empty space has to be replaced with ","
				#corpus_tm=$(echo ${corpus_tm} | tr " " ",")
				for corpus_tm_element in ${corpus_tm}
				do	if [ -n "${debug}" ]
						then	echo "curl --netrc --silent -X GET http://${server}:8080/exist/rest/apps/papyrillio/toolkit/data.xql?list=${corpus_tm_element}&folder=${corpus} > /dev/null"
						else	curl --netrc --silent -X GET "http://${server}:8080/exist/rest/apps/papyrillio/toolkit/data.xql?list=${corpus_tm_element}&folder=${corpus}" > /dev/null
					fi
				done

				if [ "${corpus}" == "DCLP"]
					then	java -Xms512m -Xmx1536m net.sf.saxon.Transform -it:OVERVIEW -xsl:/srv/data/papyri.info/git/navigator/pn-scripts/generateCorpusOverview.xsl pathTarget=/srv/data/papyri.info/pn/home/ > /dev/null
				fi
	fi
done
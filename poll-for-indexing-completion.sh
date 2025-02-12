#!/bin/bash

# Parse CURRENT_TIME argument
if [ -z "$1" ]; then
    echo "No argument supplied. Please provide the CURRENT_TIME argument.";
    exit 1;
fi;

# set argument as CURRENT_TIME
CURRENT_TIME=$1;

delay=300

while true; do
  status=$(systemctl status $service_name | grep Active | awk '{print $2}');
  if [ "$status" == "failed" ]; then
      echo "Service failed to start";
      exit 1;
  elif [ "$status" == "inactive" ]; then
      echo "Service completed";
      break;
  elif [ "$status" == "active" ] || [ "$status" == "activating" ] || [ "$status" == "deactivating" ]; then
      echo "Indexing in progress...";
      sleep $delay;
      continue;
  else
      break;
  fi;
done

sudo /usr/bin/journalctl -u papyri-navigator-indexing-playbook --since "${CURRENT_TIME}"
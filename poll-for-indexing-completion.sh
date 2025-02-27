#!/bin/bash

if [ -z "$1" ]; then
  echo "Please provide the service name as an argument."
  exit 1
fi

SERVICE_NAME=$1
delay=300

while true; do
  status=$(systemctl status $SERVICE_NAME | grep Active | awk '{print $2}');
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

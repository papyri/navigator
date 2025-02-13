#!/bin/bash

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

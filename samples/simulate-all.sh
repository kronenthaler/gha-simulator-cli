#!/bin/bash

cd $(dirname $0)

mkdir -p logs
mkdir -p data

for structure in $(find structures -name '*.yaml'); do;
  echo $structure;
  time env JAVA_OPTS="-Djava.util.logging.config.file=logging.properties" \
  ../build/install/gha-simulator-cli/bin/gha-simulator-cli \
    --output data/$(basename $structure).output \
    --print-stats \
    configs.yml \
    $structure \
    incoming.txt >> output.txt
done

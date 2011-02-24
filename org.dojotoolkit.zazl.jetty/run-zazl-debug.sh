#!/bin/sh

if [ $# -gt 0 ]; then
	java -d32 -Djava.util.logging.config.file=logging.properties -DDEBUG=true -jar library/zazljetty.jar $@
else
	java -d32 -Djava.util.logging.config.file=logging.properties -DDEBUG=true -jar library/zazljetty.jar  ../examples
fi

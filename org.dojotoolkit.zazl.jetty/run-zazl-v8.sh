#!/bin/sh

if [ $# -gt 0 ]; then
	java -d32 -Djava.util.logging.config.file=logging.properties -Djava.library.path=./ -DV8=true -jar library/zazljetty.jar $@
else
	java -d32 -Djava.util.logging.config.file=logging.properties -Djava.library.path=./ -DV8=true -jar library/zazljetty.jar ../examples
fi

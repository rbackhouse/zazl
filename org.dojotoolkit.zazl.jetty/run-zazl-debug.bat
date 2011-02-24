@echo off
IF (%1=="") (
	java -Djava.util.logging.config.file=logging.properties -DDEBUG=true -jar library/zazljetty.jar %1
) ELSE (
	java -Djava.util.logging.config.file=logging.properties -DDEBUG=true -jar library/zazljetty.jar ../examples
)
#!/bin/sh

rm -R node_modules
rm node_modules.taz.gz
mkdir node_modules
cd node_modules
mkdir jsoptimizer
mkdir zazlutil
mkdir -p zazlserver/lib/org/dojotoolkit/zazl/optimizer/tag
mkdir -p zazlserver/lib/dojox/serverdtl
cp -R ../../zazl/org.dojotoolkit.optimizer.nodejs/jsoptimizer/* ./jsoptimizer/
mkdir ./jsoptimizer/lib/optimizer/
cp -R ../../zazl/org.dojotoolkit.optimizer/optimizer/* ./jsoptimizer/lib/optimizer/
cp -R ../../zazl/org.dojotoolkit.server.util.nodejs/zazlutil/* ./zazlutil/
cp -R ../../zazl/org.dojotoolkit.zazl.nodejs/zazlserver/* ./zazlserver/
cp ../../zazl/org.dojotoolkit.zazl.optimizer/tag/jstag.js ./zazlserver/lib/org/dojotoolkit/zazl/optimizer/tag/.
cp ../../zazl/org.dojotoolkit.zazl/jssrc/dojox/serverdtl/Request.js ./zazlserver/lib/dojox/serverdtl/.
cp ../../zazl/org.dojotoolkit.zazl/jssrc/dojox/serverdtl/util.js ./zazlserver/lib/dojox/serverdtl/.
cd ..
tar cvzf node_modules.tar.gz node_modules/*
rm -R zazlnodejs
rm zazlnodejs.taz.gz
mkdir -p zazlnodejs/node_modules
cp -R ./node_modules/* ./zazlnodejs/node_modules/
mkdir -p zazlnodejs/dojo/dojo
mkdir -p zazlnodejs/dojo/dijit
mkdir -p zazlnodejs/dojo/dojox
cp -R ../targetplatform/dojo/plugins/org.dojotoolkit.dojo_1.5.0/dojo/* ./zazlnodejs/dojo/dojo/
cp -R ../targetplatform/dojo/plugins/org.dojotoolkit.dojo_1.5.0/dijit/* ./zazlnodejs/dojo/dijit/
cp -R ../targetplatform/dojo/plugins/org.dojotoolkit.dojo_1.5.0/dojox/* ./zazlnodejs/dojo/dojox/
mkdir -p zazlnodejs/samples
cp -R ../zazl/org.dojotoolkit.zazl.samples/resources/* ./zazlnodejs/samples/
cp -R ../zazl/org.dojotoolkit.zazl.nodejs/zazlserver.js ./zazlnodejs/.
mkdir -p zazlnodejs/samples/org/dojotoolkit/zazl/samples
mkdir -p zazlnodejs/samples/org/dojotoolkit/zazl/samples/handlers
mkdir -p zazlnodejs/samples/org/dojotoolkit/zazl/samples/filters
mkdir -p zazlnodejs/samples/org/dojotoolkit/zazl/samples/tags
mv ./zazlnodejs/samples/*.dtl ./zazlnodejs/samples/org/dojotoolkit/zazl/samples/
mv ./zazlnodejs/samples/*.js ./zazlnodejs/samples/org/dojotoolkit/zazl/samples/
mv ./zazlnodejs/samples/handlers/* ./zazlnodejs/samples/org/dojotoolkit/zazl/samples/handlers/
mv ./zazlnodejs/samples/filters/* ./zazlnodejs/samples/org/dojotoolkit/zazl/samples/filters/
mv ./zazlnodejs/samples/tags/* ./zazlnodejs/samples/org/dojotoolkit/zazl/samples/tags/
tar cvzf zazlnodejs.tar.gz zazlnodejs/*

 

#
# Zazl README
#

** Overview

This package contains code demonstrating using the Dojo Django
Template Language (DTL) templates in a server-side environment.

** Prereqs

The demo package has the following technology prereqs:

- A Java Runtime Environment, v5 or later
- For the V8 JavaScript runtime, Microsoft Windows or a Linux 32 bit Operating System

** Running the Server

To get started, open a command prompt, go to <package-root>/server, and type one
of the following commands:

(Windows and Linux 32bit only)> run-zazl-v8 -or- run-zazl-rhino
(Any platform)> java -jar library/zazljetty.jar ../examples

The server will start and point you to a URL to view the demo scripts. If you
want to view the source of the example scripts, they are in
<package-root>/examples.

** Running the Server in Debug Mode

To launch the Rhino version of the package in debug mode, type one of the
following commands:

(Windows onlyand Linux 32bit only)> run-zazl-debug
(Any platform)> java -DDEBUG=true -jar library/zazljetty.jar ../examples
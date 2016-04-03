#!/bin/bash

java -classpath "/usr/local/lib/antlr-4.5.1-complete.jar:bin:$$CLASSPATH" GooMain -dsym $1


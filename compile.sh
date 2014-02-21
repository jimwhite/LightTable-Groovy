#!/bin/sh

GROOVY_BIN=`which groovy`
GROOVY_ALL_JAR=`find "$GROOVY_BIN/../../embeddable" -type f ! -name "*-indy.jar" | head -n 1`
javac -cp "$GROOVY_ALL_JAR" -d classes java-src/*

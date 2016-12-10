#!/bin/sh
source /usr/local/corenlp341/classpath.sh
javac -Xlint Tokenize.java
java Tokenize

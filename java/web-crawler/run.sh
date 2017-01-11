#!/bin/bash

mvn clean compile exec:java -Dexec.args="$1 $2 $3"

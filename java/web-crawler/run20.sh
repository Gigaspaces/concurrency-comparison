#!/bin/bash

mvn  exec:java -Dexec.args="v1 http://localhost:8080 20"

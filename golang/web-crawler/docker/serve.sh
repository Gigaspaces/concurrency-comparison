#!/usr/bin/env bash

unset GOPATH
export GOPATH=/golang/web-crawler

(cd src/github.com/barakb/web-crawler/; git fetch; git rebase)

go build -o bin/web-crawler github.com/barakb/web-crawler/main

./bin/web-crawler $*

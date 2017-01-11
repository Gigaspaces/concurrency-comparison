#!/usr/bin/env bash

unset GOPATH
export GOPATH=/golang/fake-web-tree

(cd src/github.com/barakb/fake-web-tree/; git fetch; git rebase)

go build -o bin/fake-web-tree github.com/barakb/fake-web-tree/main

./bin/fake-web-tree $*

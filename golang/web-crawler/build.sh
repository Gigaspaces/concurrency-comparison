#!/usr/bin/env bash
export GOPATH=$PWD
(cd src/github.com/barakb/web-crawler; glide install)
rm -rf bin
go build -v -o bin/web-crawler github.com/barakb/web-crawler/main

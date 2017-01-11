#!/usr/bin/env bash

export GOPATH=$PWD
(cd src/github.com/barakb/fake-web-tree; glide install; glide update)
rm -rf bin

rm -f bin/fake-web-tree bin/fake-web-tree.exe

echo "building linux"
go build -o bin/fake-web-tree github.com/barakb/fake-web-tree/main

echo "building windows"
GOOS=windows GOARCH=386 go build -o bin/fake-web-tree.exe github.com/barakb/fake-web-tree/main

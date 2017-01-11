#!/usr/bin/env bash


docker run  --add-host=parent-host:`ip route show | grep docker0 | awk '{print \$9}'` -it barakb/web-crawler http://parent-host:8080


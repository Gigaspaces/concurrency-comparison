FROM golang:1.7.1
MAINTAINER Barak Bar Orion  <barak.bar@gmail.com>

RUN rm /bin/sh && ln -s /bin/bash /bin/sh


RUN apt-get install -y git

RUN curl https://glide.sh/get | sh

RUN mkdir -p /golang/fake-web-tree/src/github.com/barakb
WORKDIR /golang/fake-web-tree/src/github.com/barakb

RUN git clone https://github.com/barakb/fake-web-tree.git


WORKDIR /golang/fake-web-tree/src/github.com/barakb/fake-web-tree

env PATH /golang/fake-web-tree/src/github.com/barakb/fake-web-treKe:$PATH

RUN glide install

WORKDIR /golang/fake-web-tree

COPY serve.sh /golang/fake-web-tree/

EXPOSE 8080

ENTRYPOINT ["/golang/fake-web-tree/serve.sh"]

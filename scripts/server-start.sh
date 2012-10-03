#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

shopt -s huponexit
bash $SELF/dist-crawl-server.sh -l $SELF/worker.log $* > $SELF/out.txt 2> $SELF/err.txt &
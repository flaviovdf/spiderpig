#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

java -cp $SELF/out/'*':$SELF/lib/'*' -Djava.rmi.server.hostname=`hostname` br.ufmg.dcc.vod.ncrawler.ui.CollectClient $*
#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

java -cp $SELF/out/'*':$SELF/lib/'*' br.ufmg.dcc.vod.ncrawler.ui.CollectServerKiller $*
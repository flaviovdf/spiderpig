#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

cd $SELF
    tar czf pack.tar.gz lib out *.sh > /dev/null 2> /dev/null
cd - > /dev/null
exit 0

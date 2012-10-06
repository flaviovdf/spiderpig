#!/bin/bash

findself() {
    SELF=`dirname $0`
}
findself

if [ $# -lt 1 ]; then
    exit 1
fi

cd $1
    tar zxf pack.tar.gz > /dev/null
cd - > /dev/null
exit 0

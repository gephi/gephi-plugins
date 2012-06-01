#!/bin/bash

file=$1

lastline=`tail -1 $file`
check=`echo $lastline | awk '{print $2}'`
if [ x$check != xmin ]; then
    echo "error on last line"
    exit 1
fi
min=`echo $lastline | awk '{print $3}'`
grep "min $min " $file | head -1 | awk '{for(i=5;i<=NF-1;i++){printf "%d ", $i}printf "\n"}'

stat_file=`basename $file .log`.stat
cost=`echo $file | awk -F'-' '{print $2}' | cut -c 2-`

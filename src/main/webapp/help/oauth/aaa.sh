#!/usr/bin/env bash

for i in `find . -name '*.zh_CN.html'`
do
  ii=`echo $i | cut -f2 -d '.'`
  mv .$ii.zh_CN.html .${ii}_zh_CN.html
done
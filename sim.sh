#!/bin/bash

for((i = 10; i < 31; i++));
do
  ./Main 1 $i 0.04 0.01
  cp sim.data /data/1-$i-004.data
done
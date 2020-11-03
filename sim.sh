#!/bin/bash

for((i = 10; i < 31; i++));
do
  java ./src/cpuscheduler/Main 1 $i 0.04 0.01
  cp ./Data/sim.data ./Data/1-$i-004.data
done

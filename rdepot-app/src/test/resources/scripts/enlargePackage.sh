#!/bin/bash
PACKAGES_DIR=$1
PACKAGE_FILE=$2
cd $PACKAGES_DIR
tar -xvzf $PACKAGE_FILE
head -c 500M </dev/urandom > Benchmarking/random.txt
mv Benchmarking BigBenchmarking
tar -czvf Big$PACKAGE_FILE BigBenchmarking/*
rm -rf BigBenchmarking

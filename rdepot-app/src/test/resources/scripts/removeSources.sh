#!/bin/bash

LOCATION=$(dirname "$(readlink -f -)");
cd $LOCATION/..
rm -rf itestGenerated itestNewFiles itestPackages itestServer itestSourceFiles itestPdf

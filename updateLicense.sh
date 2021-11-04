#! /bin/bash

# First argument is a year to be replaced
# Second argument is a year which should be placed in file

find . -name "*.java" -print | xargs -i@ sed -i "s/Copyright (C) 2012-$1 Open Analytics NV/Copyright (C) 2012-$2 Open Analytics NV/g" @

echo "licenses updated"

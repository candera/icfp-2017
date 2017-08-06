#!/usr/bin/env bash

set -e

TEAM_ID=cc6ce541-f7a7-4e15-b591-adb289e5b31b
SUBMISSION_FILE=icfp-$TEAM_ID.tar

rm -f $SUBMISSION_FILE.gz

tar -cvf $SUBMISSION_FILE src/
tar -v --append --file=$SUBMISSION_FILE README
tar -v --append --file=$SUBMISSION_FILE PACKAGES
tar -v --append --file=$SUBMISSION_FILE -C scripts/ install
tar -v --append --file=$SUBMISSION_FILE -C scripts/ punter

echo "Building .jar..."
boot build

tar -v --append --file=$SUBMISSION_FILE -C target/ punter.jar

gzip -v $SUBMISSION_FILE

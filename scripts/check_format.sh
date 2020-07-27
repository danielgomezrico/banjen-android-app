#!/usr/bin/env bash
#
# Runs dartfmt from dart_style to check if all of the source files are well formatted
# Requires you to have dartfmt already in your path with 'pub global activate dart_style'
#

echo "Running dartfmt to check project dart style"

RESULT=$(~/.pub-cache/bin/dartfmt  . -n)

if [[ $? != 0 ]]; then
    echo "----> Command failed."
elif [[ $RESULT ]]; then
    echo "----> Something is looking weird here :O"
    exit 1
else
    echo "----> All format is good :)"
fi
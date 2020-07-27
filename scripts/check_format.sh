#!/usr/bin/env bash
#
# Runs dartfmt from dart_style to check if all of the source files are well formatted
# Requires you to have dartfmt already in your path with 'pub global activate dart_style'
#

RESULT=$(~/.pub-cache/bin/dartfmt  . -n)

if [[ $? != 0 ]]; then
    echo "Command failed."
elif [[ $RESULT ]]; then
    echo "Something is looking weird here :O"
else
    echo "All format is good :)"
fi
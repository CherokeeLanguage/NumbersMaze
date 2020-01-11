#!/bin/sh

cd "$(dirname "$0")" || exit 1

cp /dev/null plist.txt
ls -1 *.ogg > plist.txt

cp /dev/null plist-levels.txt
ls -1 level-songs/*.ogg > plist-levels.txt

exit 0

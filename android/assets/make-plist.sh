#!/bin/sh

cd "$(dirname "$0")" || exit 1

cp /dev/null plist.txt

ls -1 number-tiles/*.png >> plist.txt
ls -1 720p/*.png >> plist.txt
ls -1 9patch/*.png  >> plist.txt
ls -1 background/*.png >> plist.txt

exit 0

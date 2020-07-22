#!/bin/sh

cd "$(dirname "$0")" || exit 1

rm plist.txt || true
ls -1 720p/*.png |grep -v 'plist.txt' > plist.txt
ls -1 9patch/*.png | grep -v 'plist.txt' >> plist.txt
ls -1 background/*.png | grep -v 'plist.txt' >> plist.txt
exit 0

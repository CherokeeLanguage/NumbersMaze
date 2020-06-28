#!/bin/bash
cp /dev/null plist.txt
for x in *png; do
	echo "720p/${x}" >> plist.txt
done

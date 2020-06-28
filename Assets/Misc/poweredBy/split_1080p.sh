#!/bin/sh

width=1920
height=1080

i=0
for y in $(seq 0 125 624); do
	for x in $(seq 0 384 1919); do
		echo gm convert -crop 384x125+${x}+${y} powered_by_libGDX_1080p.png 1080p_${i}.png
		gm convert -crop 384x125+${x}+${y} powered_by_libGDX_1080p.png 1080p_${i}.png
		i=$((${i}+1))
	done
done

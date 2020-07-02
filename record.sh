#!/bin/bash

set -e
set -o pipefail

trap "echo; echo ERROR; echo" ERR

cd "$(dirname "$0")"

./gradlew desktop:dist || true

length="0:10:00"

wmctrl -c "Cherokee Numbers Maze" || true
id="$(wmctrl -l | grep "Cherokee Numbers Maze" | grep Session | cut -f 1 -d ' ' | head -n 1)" || true
if [ "$id"x != x ]; then wmctrl -i -c "$id"; fi

OUT="Numbers-Maze.mp4"
if [ -f "$OUT" ]; then rm "$OUT"; fi

echo "Running App"
(
	java -jar desktop/build/libs/*.jar
) &

sleep 1
count=10
nextwindow=0
while [ "$(wmctrl -l | grep 'Cherokee Numbers Maze')"x = x ]; do
	sleep 1;
	count=$(($count-1))
	if [ "$count" = "0" ]; then
		nextwindow=1
		break
	fi
done
id="$(wmctrl -l | grep 'Cherokee Numbers Maze' | cut -f 1 -d ' ' | head -n 1)"
wmctrl -i -r "$id" -e 0,2540,20,1280,720

#pactl list sources|less
AUDIO1="alsa_input.pci-0000_00_1b.0.analog-stereo"
AUDIO2="alsa_output.pci-0000_00_1b.0.analog-stereo.monitor"
RESYNC1="aresample=async=1:min_hard_comp=0.100000:first_pts=0"
RESYNC2="aresample=async=10000"
pacmd set-default-source "${AUDIO2}"

#https://launchpad.net/~jon-severinsson/+archive/ffmpeg
ffmpeg -f alsa -ac 2 -i pulse -f x11grab -framerate 30 -s 1280x720 \
	-i "${DISPLAY}+2545,47" -crf 24 -af "$RESYNC2" -t "$length" "$OUT"

wmctrl -i -c "$id"
id="$(wmctrl -l | grep 'Cherokee Numbers Maze' | cut -f 1 -d ' ' | head -n 1)"
if [ "$id"x != x ]; then wmctrl -i -c "$id"; fi


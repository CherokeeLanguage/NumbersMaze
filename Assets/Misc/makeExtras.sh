#!/bin/bash

#DejaVu Book: /usr/share/fonts/truetype/ttf-dejavu/DejaVuSans.ttf
#FreeSerif Medium: /usr/share/fonts/truetype/freefont/FreeSerif.ttf
#unifont: /usr/share/fonts/truetype/unifont/unifont.ttf

cd "$(dirname "$0")" || exit 1

F1="/usr/share/fonts/truetype/ttf-dejavu/DejaVuSans.ttf"

#for density in 240p 480p 720p 1080p; do
#for density in 720p; do
DEST=720p
if [ ! -d "${DEST}" ]; then mkdir "${DEST}"; fi

for x in "${DEST}"/*; do
    if [ -f "$x" ]; then rm "$x"; fi
done

ix=0

tileSize=30 #24

workSize=$(($tileSize * 3))

echo "TILESIZE: $tileSize"

for glyph in - _ = + +0 -0 ✘ ✓ ✔ ✕ ✖ ✗ ✘ ♫ ☐ ☑ ☒ ☓ ☠ ☹ ☺ 0 +1 +2 +3 +4 +5 -1 -2 -3 -4 -5; do
    #for color in crimson firebrick blue red green purple orange yellow black brown gold2 gold3 cornsilk4 white; do
    for color in black; do
        file="_${glyph}_${color}".png
        file="${ix}".png
        gm convert \
            -background white \
            -fill $color \
            -stroke none \
            -strokewidth 0 \
            -font "$F1" \
            -size ${workSize}x${workSize} \
            label:"$glyph" \
            -trim \
            "PNG32:${DEST}"/"$file"  
        gm mogrify -background none -gravity center -resize ${tileSize}x${tileSize} -extent ${tileSize}x${tileSize} "${DEST}"/"$file"
        ix=$(($ix+1))
    done
done

ix=1
for glyph in ⚀ ⚁ ⚂ ⚃ ⚄ ⚅; do
    #for color in crimson firebrick blue red green purple orange yellow black brown gold2 gold3 cornsilk4 white; do
    for color in black; do
        file="_${glyph}_${color}".png
        file="${ix}".png
        gm convert \
            -background white \
            -fill $color \
            -stroke none \
            -strokewidth 0 \
            -font "$F1" \
            -size ${workSize}x${workSize} \
            label:"$glyph" \
            -trim \
            "PNG32:${DEST}"/"d${file}"  
        gm mogrify -background none -gravity center -resize ${tileSize}x${tileSize} -extent ${tileSize}x${tileSize} "${DEST}"/"d${file}"
        ix=$(($ix+1))
    done
done

ls -1 "${DEST}"/*.png > "${DEST}"/plist.txt

cd "${DEST}"
xdg-open .

#!/bin/bash

#DejaVu Book: /usr/share/fonts/truetype/ttf-dejavu/DejaVuSans.ttf
#FreeSerif Medium: /usr/share/fonts/truetype/freefont/FreeSerif.ttf
#unifont: /usr/share/fonts/truetype/unifont/unifont.ttf

cd "$(dirname "$0")" || exit 1

height=58

F1="/usr/share/fonts/truetype/ttf-dejavu/DejaVuSans.ttf"
DEST=player-58px
SRC=player-32px
if [ ! -d "${DEST}" ]; then
	mkdir "${DEST}"
fi

#try and deal with the ehance docker image requiring root!
chmod g+s "${DEST}"
setfacl -m "default:group::rwx" "${DEST}"

if [ ! -d "${SRC}" ]; then
	echo "${SRC}"
	echo error
	read a
	exit 1
fi
if [ ! -d "${DEST}" ]; then
	echo "${DEST}"
	echo error
	read a
	exit 1
fi

rm "$DEST"/*.png
rm "$DEST"/*.tiff

#See https://github.com/alexjc/neural-enhance#1-examples--usage
function enhance() {
	docker run --rm --user "0:$(id -g)" -v "$(pwd)/`dirname ${@:$#}`":/ne/input -it alexjc/neural-enhance ${@:1:$#-1} "input/`basename ${@:$#}`"; 
}

xdg-open "${DEST}"

for avatar in "${SRC}"/*.png; do
	file="$(basename "${avatar}")"

	echo "Enhancing: ${file}"

	cp "${avatar}" "${DEST}/${file}"
	#enhance --zoom=2 "${DEST}/${file}"
	#mv -v "${DEST}/${file}"

	gm convert \
		-fill none \
		-background none \
		-stroke none \
		-type TrueColorMatte \
		-resize ${height}x${height} \
		-filter Cubic \
		"${avatar}" \
		"${DEST}/${file}"
		
	#gm mogrify -background none -gravity center -resize ${tileSize}x${tileSize} -extent ${tileSize}x${tileSize} "${DEST}"/"$file"
	#ix=$(($ix+1))
done

ls -1 "${DEST}"/*.png > "${DEST}"/plist.txt

echo "done"
read a




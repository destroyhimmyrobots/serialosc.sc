#!/bin/sh
SYS="$(uname)"
EXTS=""
COPY=0

case "$SYS" in
	Darwin)
		echo "You are using" "$SYS"
		EXTS="$HOME""/Library/Application Support/SuperCollider/Extensions/org.monome.echoes.serialosc.sc"
		COPY=1
		;;
	Linux)
		echo "You are using" "$SYS"
		EXTS="$HOME""/.local/share/SuperCollider/Extensions/org.monome.echoes.serialosc.sc"
		COPY=1
		;;
	*)
		echo "Issue ( Platform.userExtensionDir; ) to SC. Then, copy these files into the posted directory."
		;;
esac

if [ "$COPY" -ne 0 ]; then
	if [ ! -d "$EXTS" ]; then
		mkdir "$EXTS"
		echo "Created extension directory."
	fi
	
	find . -name "*.sc" -exec cp {} "$EXTS" \;
	echo  "Finished copying files:"
	ls "$EXTS"
fi

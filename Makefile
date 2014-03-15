# Terribly basic make file to build pebble and android apps

all: pebble_install android_start

pebble_build:
	cd pebble_accel_dump && pebble build

pebble_install: pebble_build
	cd pebble_accel_dump && pebble install

android_build:
	cd AccelDump && ant debug

android_install: android_build
	cd AccelDump && adb install -r bin/AccelDump-debug.apk

android_start: android_install
	adb shell am start -n com.whitneyindustries.acceldump/.DumpActivity

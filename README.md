pebble-accel-analyzer
=====================

Skeleton pebble + android + flask app to just pull AccelData from pebble watch to a DB.  For use in analyzing accelerometer data.

Currently using MongoDB as the backend for storing the accelerometer data.

Each component currently builds and installs with some work on my setup.  Probably will not work out of the box for others yet, will check in build scripts, virtualenv stuff, etc. soon.

Requirements
------------

- MongoDB 2.x server
- Pebble SDK 2.0.1+
- Stuff in requirements.txt

You will need to link PebbleKit-Android/PebbleKit from the Pebble SDK to the top level directory of this repo so that AccelDump can access it.

Build
-----

- Build pebble watch app and install
- Build android app and install
- Start data sink server http2mongo.py

Running `make` in the top level dir will do the first 2 steps, assuming you have everything setup correctly:

- Single android device setup in `adb devices`
- Pebble android app installed on device with Developer Connection enabled
- PEBBLE_PHONE IP address set


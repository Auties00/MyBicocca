#!/bin/sh
# Extracts the basemap of the campus area the app's map shows, from Protomaps' daily planet build,
# into the app's assets. Needs the pmtiles CLI: https://github.com/protomaps/go-pmtiles
set -e
cd "$(dirname "$0")/../.."
pmtiles extract https://build.protomaps.com/20260610.pmtiles app/src/main/assets/map/basemap.pmtiles --bbox=9.189,45.491,9.653,45.710 --maxzoom=14 --download-threads=8

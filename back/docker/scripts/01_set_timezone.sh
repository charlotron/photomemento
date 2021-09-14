#!/bin/bash
if [ -z "$TZ" ]; then
  TZ=Europe/London
fi;
echo "Setting timezone to $TZ"
ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

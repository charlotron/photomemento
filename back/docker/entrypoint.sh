#!/bin/bash

cd "$APP_RELATIVE" || die
java -Dspring.profiles.active=prod -jar app.jar
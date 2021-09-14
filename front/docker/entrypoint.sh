#!/bin/sh

cd "$DOCKER_APP_ROOT" || die

nginx -g "daemon off;"

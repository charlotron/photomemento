#!/bin/sh

#DO NOT EXECUTE THIS ON YOUR LOCAL MACHINE


#Executing init scripts
echo "------ Initializing and configuring service"
find scripts -type f -name "*.sh" | sort | xargs -I {} sh -c 'echo "------ Running script: {}"; sh {}'
#Running app
echo "------ Starting app.."
sh "./entrypoint.sh"

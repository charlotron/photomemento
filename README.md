#Photomemento

## What is?
A full media gallery with the following features:
 * Photo/Video gallery preview, Google Photos inspired with the following sub-features
    * Gallery view, photo and video view, with slideshow
    * Video preview 5 seconds that allows pre-watch how the video will be
    * Resampling of photo/video for easier and faster media display keeping untouched original multimedia
 * Directory based gallery preview, you can navigate in your directory structure and view the multimedia sorted as you want (still not exists in Google Photos)    
 * Ability to geo positioning photos/videos having metadata with location (coordinates), using OpenStreetMap
 * Map view, displaying locations where photos where shot based on geo positioning data. Then you can go to the photos of given location.
 * Search photos, by name, location, folder, etc.
 * Management and stat utils to check the overall system status and progress of processing.
 * Instant detection of changes in directories (new photo was put in a folder, a photo was removed, etc.)

## What is in the future? (Roadmap)
 * Security layer to protect access to your gallery with Spring Security. (It can be achieved right now using ngingx)
 * Dark mode, a more eye friendly style for the gallery
 * Manage photos (remove photos, rotate, change location, etc.)
 * Configuration section, to change settings inside the application.  
 * A better way to view photos in a given date (ie, march 2005)
 * Backup configuration/photo database
 * AI to resolve people, things, etc.

## WHERE CAN SHOULD BE INSTALLED?

You can install this program in any computer with docker working (if you have proper ram memory), but it is meant to process photo/videos slowly over time, so I recommend running on a server/nas.

For example in an "HPE ProLiant Gen10" (with an AMD cpu and 4gb of ram), and around 26k photos with 1k videos it takes around 1,5 days in process and indexing everything. (the slowest process is geo-resolving position and video processing)

## MINIMAL REQUIREMENTS

Any computer with "Docker" installed and with at least 2Gb RAM, this implies should allow "os virtualization". You also, need docker-compose but now it is included with docker so it is fine (just ensure you have a proper verson of docker)

Just try to install docker, if you can run it, then it will be fine.

## INSTALL

#### STEP 1: INSTALL DOCKER
###### UBUNTU
```
#Update apt-get
sudo apt-get update

#Install docker
sudo apt-get install docker-ce docker-ce-cli containerd.io
```
###### MACOS
```
#Install brew (just a program manager)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"

#Install docker
brew cask install docker
```
###### MANUAL WAY
Just go to https://www.docker.com/get-started, and follow docker instructions.
`
### STEP 2: Get current userId and groupId

This step is only needed in unix based systems(max/linux), we need the user id and group id for the user it is going to run the program to allow:

* Type this in a terminal, with the user you want to run the program
```
id
#Sample response uid=XXXXX(nameofuser) gid=YYYYY(nameofgroup) [...]
```
* Note down the uid and gid, you will need it later as PUID and PGID

### STEP 3: CREATE DIRECTORY STRUCTURE

This example uses base path /docker to install the program in, you can choose the base path that you want

* Create a base path to keep the docker data (with the same you run previous step)
```
mkdir -p /docker/photomemento/back/cache    #This is just a directory to be used to generate and store the thumbnails, previews, etc..
mkdir -p /docker/photomemento/back/data     #The directory where you will put the photos/videos, this also can be a symbolik link
mkdir -p /docker/photomemento/back/logs     #Just a folder where the logs will be written
mkdir -p /docker/photomemento/db/data       #In this folder the database will be stored
chmod 755 /docker/photomemento -R           #Allow writting on directory
```

### STEP 4: Configure and run the program

* Create a file called "docker-compose.yml" in the base path, ie in OSX:
```
touch /docker/photomemento/docker-compose.yml
```
* Edit the file with your favorite editor and paste the following:
```
version: '3'

services:

  db:
    mem_swappiness: -1
    image: mongo
    restart: unless-stopped
    volumes:
      - /docker/photomemento/db/data:/data/db

  back:
    mem_swappiness: -1
    image: charlotron/photo_memento_back:latest
    container_name: photo-memento-back
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - PUID=YYYYY                                      #Change this by your PUID (from step 2)
      - PGID=XXXXX                                      #Change this by your PGID (from step 2)
      - TZ=Europe/Madrid
    volumes:
      - /docker/photomemento/back/data:/data
      - /docker/photomemento/back/cache:/cache
      - /docker/photomemento/back/logs:/app/app/logs
    depends_on:
      - db

  front:
    mem_swappiness: -1
    image: charlotron/photo_memento_front:latest
    container_name: photo-memento-front
    restart: unless-stopped
    ports:
      - "80:80"                                         #You can change this port to listen in another port
    environment:
      - TZ=Europe/London
      - NG_BACKEND_BASE_URL=http://localhost:8080       #You can change this if your server is in another url different than localhost (ie: you have a subdomain)
    depends_on:
      - back
```
(dont forget to update PUID and PGID)

### STEP 5: Run the docker stack

Execute in the same directory the following:
```
docker compose up
```

Wait till finished waking up then open http://localhost

If you want to deploy the service in background, run the following:
```
docker compose up -d
```
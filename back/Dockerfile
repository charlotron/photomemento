# ======== COMPILE/BUILD ===============
FROM adoptopenjdk/openjdk11:debian-slim as back-build-step

#Constants
ARG APP_RELATIVE=app
ARG BUILD_ROOT=/$APP_RELATIVE
ARG BUILD_DIST_ROOT=$BUILD_ROOT/dist
ARG BUILD_DIST_ROOT_APP=$BUILD_DIST_ROOT/$APP_RELATIVE

#Install maven
RUN apt-get update
RUN apt-get install -y maven

#Copy resources to /app
RUN mkdir -p /app
COPY src /app/src
COPY pom.xml /app
COPY docker /app/docker

WORKDIR $BUILD_ROOT

#Generate dist
RUN mvn clean package

RUN mkdir -p $BUILD_DIST_ROOT_APP
RUN mv /app/target/*.jar $BUILD_DIST_ROOT_APP/app.jar
RUN mv /app/src/main/resources/* $BUILD_DIST_ROOT_APP
RUN mv /app/docker/* $BUILD_DIST_ROOT
#Clean dev data
RUN find $BUILD_DIST_ROOT_APP -name "*-dev*" -delete

## ======== PREPARE: DIST IMAGE ===============
FROM adoptopenjdk/openjdk11:debian-slim

#Constants
ARG USER_NAME=photo-memento
ARG GROUP_NAME=photo-memento
ARG HOME=/home/$USER_NAME
ARG APP_RELATIVE=app
ARG DOCKER_APP_ROOT=/$APP_RELATIVE
ARG DATA_APP_ROOT=/data
ARG CACHE_ROOT=/cache
ARG CACHE_THUMBS=$CACHE_ROOT/thumbs
ARG APP_ROOT=$DOCKER_APP_ROOT/$APP_RELATIVE
ARG DEFAULT_PUID=95
ARG DEFAULT_PGID=95

#Setting some constants as ENV variables to be used inside the container
ENV DOCKER_APP_ROOT=$DOCKER_APP_ROOT
ENV APP_ROOT=$APP_ROOT
ENV APP_RELATIVE=$APP_RELATIVE
ENV USER_NAME=$USER_NAME
ENV GROUP_NAME=$GROUP_NAME

#Install ffmpeg
RUN apt-get update
RUN apt-get install -y ffmpeg

RUN echo "Creating User: $USER_NAME (id: $DEFAULT_PUID)"
RUN echo "Creating Group: $GROUP_NAME (id: $DEFAULT_PGID)"

#Define some labels
LABEL maintainer="Carlos Cenjor Ramírez"
LABEL team="Photo memento team"
LABEL description="Photo memento container"

#Fix permissions uid to match host ones
RUN groupadd -r -g $DEFAULT_PGID $GROUP_NAME
RUN useradd -rm -d $HOME -s /bin/bash -g $GROUP_NAME -u $DEFAULT_PUID $USER_NAME
RUN usermod -u $DEFAULT_PUID $USER_NAME
RUN groupmod -g $DEFAULT_PGID $GROUP_NAME

#Prepare directory structure
RUN mkdir -p $DOCKER_APP_ROOT
RUN chmod 755 $DOCKER_APP_ROOT -R
RUN mkdir -p $DATA_APP_ROOT
RUN chmod 755 $DATA_APP_ROOT -R
RUN mkdir -p $CACHE_ROOT
RUN mkdir -p $CACHE_THUMBS
RUN chmod 755 $CACHE_ROOT

#Copy whole project to destination directory
COPY --from=back-build-step /app/dist $DOCKER_APP_ROOT

#Fix permissions for moved files
RUN chown -R $USER_NAME:$GROUP_NAME $DOCKER_APP_ROOT
RUN chmod 755 $DOCKER_APP_ROOT -R

WORKDIR $DOCKER_APP_ROOT

#Configure and run
ENTRYPOINT ["./init.sh"]
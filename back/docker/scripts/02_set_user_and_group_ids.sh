#!/bin/bash

#DO NOT EXECUTE THIS ON YOUR LOCAL MACHINE

if [ ! -z "$PUID" ]; then
  if [ ! -z "$PGID" ]; then
      #===== USER ===========
      #Save
      LINE_USER=$(cat /etc/passwd | grep "$USER_NAME:")
      #Ensure ID is not set to another user
      PASSWD_WO_USERS=$(cat /etc/passwd | grep -v ":$PUID:$PGID:" | grep -v "$USER_NAME")
      echo "$PASSWD_WO_USERS" > /etc/passwd
      #Fill new user
      echo "$LINE_USER" >> /etc/passwd
      #Change user id
      usermod -u "$PUID" "$USER_NAME"
      echo "Updating User: $USER_NAME (id: $PUID)"

      #===== GROUP ===========
      #SAVE
      LINE_GROUP=$(cat /etc/group | grep "$GROUP_NAME:")
      #Ensure ID is not set to another group
      PASSWD_WO_GROUP=$(cat /etc/group | grep -v ":$PGID:" | grep -v "$GROUP_NAME")
      echo "$PASSWD_WO_GROUP" > /etc/group
      #Fill new user
      echo "$LINE_GROUP" >> /etc/group
      #Change user id
      groupmod -g "$PGID" "$GROUP_NAME"
      echo "Updating Group: $GROUP_NAME (id: $PGID)"
  fi;
fi;
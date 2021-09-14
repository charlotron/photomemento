#!/bin/sh

ENV_FILE_LOCAL=/app/html/assets/env.local.json #TODO: NOT TAKING $APP_HTMLS arg why?

echo "{">"$ENV_FILE_LOCAL"
for NAME in $(awk "END { for (name in ENVIRON) { print name; }}" < /dev/null)
do
  VAL="$(awk "END { printf ENVIRON[\"$NAME\"]; }" < /dev/null)"
  case $NAME in
    "NG_"*)
      NAME=${NAME#NG_};
      echo "Detected angular env var: \"$NAME\" with value \"$VAL\""
      echo "\"$NAME\":\"$VAL\"">>"$ENV_FILE_LOCAL" ;;
  esac
done
echo "}">>"$ENV_FILE_LOCAL"

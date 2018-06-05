#!/bin/bash
echo Prepare MockServer responses

json_escape_file () {
    [ -f "$1" ] || (echo "File not found! $1"; return 1)
    cat "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

for file in $(find ./mockserver/ -name '*.ok'); do
  filename=$(basename $file)
  id="${filename%.*}"
  echo Add $id

  URL="/v1/address/email/$id"
  BODY="$(json_escape_file $file)"

  curl -sX PUT "http://localhost:1080/expectation" -d "{
  httpRequest : {
    path: \"$URL\"
  },
  httpResponse : {
    body: $BODY
  },
  times: {
    unlimited: true
  },
  timeToLive: {
    unlimited: true
  }
}"
done

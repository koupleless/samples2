#!/bin/sh

DEST_VERSION=$1

# 如果 DEST_VERSION 为空，则报错
if [ -z "$DEST_VERSION" ]; then
  echo "Error: DEST_VERSION must be provided."
  exit 1
fi

# git grep -rn '2.2.12' | cat | awk -F: '{print $1}' | xargs -I {} sed -i '' 's/2.2.12/2.2.13/g' {}
grep -rn --include="*.xml" "<koupleless.runtime.version>2\..*</koupleless.runtime.version>" | cat | awk -F: '{print $1}' | xargs -I {} sed -i '' "s/\<koupleless.runtime.version\>2\..*\<\/koupleless.runtime.version\>/\<koupleless.runtime.version\>$1\<\/koupleless.runtime.version\>/g" {}

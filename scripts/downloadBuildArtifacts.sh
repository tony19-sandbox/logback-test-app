#!/bin/bash -ex

build_number=${_CIRCLE_BUILD_NUM}
username=tony19
project=logback-android

artifactUrl=$(curl https://circleci.com/api/v1.1/project/github/$username/$project/$build_number/artifacts?circle-token=$CIRCLE_TOKEN | jq '.[] | select(.path=="outputs/aar/logback-android-debug.aar") | .url')

temp="${artifactUrl%\"}"
artifactUrl="${temp#\"}"

mkdir -p app/libs
curl $artifactUrl --output app/libs/logback-android.aar
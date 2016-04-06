#!/bin/sh

# only run the script if packages/TestDPC is in the current directory path.
pwd | grep -q "packages/TestDPC$"

# if this failed (returned 1), prompt the user.
if [ $? == 1 ]; then
    echo "Please run this script from the TestDPC project folder."
   exit 1
fi

cd ../../../..
# Assume we are always building TestDpc in linux.
LOCAL_SDK_DIR=prebuilts/fullsdk/linux

echo sdk.dir=$(pwd -P)/$LOCAL_SDK_DIR > 'local.properties'

#!/bin/bash

# Script to take a screenshot from an Android device, pull it, and then remove it from the device.
# The filename for the screenshot is a required parameter.

# --- Configuration ---
# The directory on the Android device where the screenshot will be temporarily saved.
REMOTE_DIR="/sdcard/Download"

# --- Argument Check ---
# Check if exactly one argument (the filename) is provided.
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <filename.png>"
    echo "Example: $0 my_screenshot.png"
    exit 1
fi

# --- Variables ---
# The desired filename for the screenshot, taken from the first script argument.
FILENAME="$1"
# The full path to the screenshot file on the Android device.
REMOTE_FILE_PATH="${REMOTE_DIR}/${FILENAME}"
# The local path where the screenshot will be pulled. Defaults to the current directory.
LOCAL_DESTINATION="."

# --- Helper Function for ADB Commands ---
# This function executes an adb command and checks for errors.
# If an error occurs, it prints a message and exits the script.
execute_adb_command() {
    echo "Executing: adb $@"
    adb "$@"
    # $? holds the exit status of the last executed command.
    # A non-zero exit status usually indicates an error.
    if [ "$?" -ne 0 ]; then
        echo "Error: 'adb $@' failed."
        # Optional: If a partial file was created on the device and pull failed,
        # you might want to attempt to clean it up.
        # echo "Attempting to clean up remote file: ${REMOTE_FILE_PATH}"
        # adb shell rm "${REMOTE_FILE_PATH}" > /dev/null 2>&1
        exit 1
    fi
}

# --- Main Script Logic ---

echo "Starting screenshot process..."

# 1. Capture the screenshot on the Android device.
#    The '-p' flag indicates that the output should be a PNG.
execute_adb_command shell screencap -p "${REMOTE_FILE_PATH}"
echo "Screenshot captured on device: ${REMOTE_FILE_PATH}"

# 2. Pull the screenshot from the device to the local machine.
#    The '.' means pull to the current directory with the same filename.
execute_adb_command pull "${REMOTE_FILE_PATH}" "${LOCAL_DESTINATION}/${FILENAME}"
echo "Screenshot pulled to local machine: ${LOCAL_DESTINATION}/${FILENAME}"

# 3. Remove the screenshot from the device to clean up.
execute_adb_command shell rm "${REMOTE_FILE_PATH}"
echo "Screenshot removed from device: ${REMOTE_FILE_PATH}"

echo "Screenshot process completed successfully!"
exit 0


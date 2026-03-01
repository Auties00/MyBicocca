#!/bin/bash

# Force UTF-8 encoding to prevent grep -P errors
export LC_ALL=C.UTF-8

BASE_DOMAIN="https://s3w.si.unimib.it"
CONFIG_URL="${BASE_DOMAIN}/e3rest/api/swagger-service-v1/swagger/apis"

echo "Fetching API list..."

# Store curl output in a variable for debugging if necessary
RAW_DATA=$(curl -s "$CONFIG_URL")

# Extract URLs
URLS=($(echo "$RAW_DATA" | grep -oP '"location":\s*"\K[^"]+' | sed "s|^/|$BASE_DOMAIN/|"))

if [ ${#URLS[@]} -eq 0 ]; then
    echo "Error: No URLs found."
    echo "Server response:"
    echo "${RAW_DATA}..."
    exit 1
fi

echo "Found ${#URLS[@]} APIs."
#!/bin/bash
# Exports OpenAPI spec from running Docker app to ./openapi.json

set -e

OPENAPI_URL="http://localhost:8082/v3/api-docs"
OUTPUT_FILE="openapi.json"

# Wait for the app to be available (simple check, max 30s)
for i in {1..30}; do
  if curl --silent --fail "$OPENAPI_URL" > /dev/null; then
    break
  fi
  echo "Waiting for app container to be up... ($i)"
  sleep 1
  if [ "$i" -eq 30 ]; then
    echo "App not available at $OPENAPI_URL after 30s. Exiting."
    exit 1
  fi
 done

# Download the OpenAPI spec
curl --silent "$OPENAPI_URL" -o "$OUTPUT_FILE"

if [ -f "$OUTPUT_FILE" ]; then
  echo "OpenAPI spec exported to $OUTPUT_FILE"
else
  echo "Failed to export OpenAPI spec."
  exit 2
fi

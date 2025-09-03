#!/bin/bash
# Exports OpenAPI spec from running Docker app to scripts/openapi.json and scripts/openapi.yaml

set -e

OPENAPI_URL="http://localhost:8082/v3/api-docs"
OUTPUT_DIR="../scripts"
OUTPUT_JSON="$OUTPUT_DIR/openapi.json"
OUTPUT_YAML="$OUTPUT_DIR/openapi.yaml"

# Ensure output directory exists
mkdir -p "$OUTPUT_DIR"

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

# Download OpenAPI spec (JSON)
curl --silent "$OPENAPI_URL" -o "$OUTPUT_JSON"

if [ -f "$OUTPUT_JSON" ]; then
  echo "OpenAPI spec exported to $OUTPUT_JSON"
else
  echo "Failed to export OpenAPI spec."
  exit 2
fi

# Convert JSON to YAML
echo "Converting $OUTPUT_JSON to $OUTPUT_YAML..."
if command -v yq > /dev/null 2>&1; then
  yq -P . "$OUTPUT_JSON" > "$OUTPUT_YAML"
  echo "OpenAPI spec also exported to $OUTPUT_YAML using yq."
elif command -v python3 > /dev/null 2>&1; then
  VENV_DIR=".openapi-venv"
  if [ ! -d "$VENV_DIR" ]; then
    python3 -m venv "$VENV_DIR"
    source "$VENV_DIR/bin/activate"
    pip install pyyaml
  else
    source "$VENV_DIR/bin/activate"
  fi
  python -c 'import sys, json, yaml; yaml.safe_dump(json.load(sys.stdin), sys.stdout, sort_keys=False)' < "$OUTPUT_JSON" > "$OUTPUT_YAML"
  deactivate
  echo "OpenAPI spec also exported to $OUTPUT_YAML using Python/venv."
else
  echo "Could not convert JSON to YAML: yq or Python3 required. Please install yq (https://github.com/mikefarah/yq) or Python."
  exit 3
fi

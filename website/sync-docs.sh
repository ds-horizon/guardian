#!/bin/bash
# Script to sync docs from parent directory to Astro content directory

# Create directories if they don't exist
mkdir -p src/content/docs/configuration
mkdir -p src/content/docs/features

# Copy markdown files
cp ../docs/configuration/*.md src/content/docs/configuration/ 2>/dev/null || true
cp ../docs/features/*.md src/content/docs/features/ 2>/dev/null || true

echo "Docs synced successfully"


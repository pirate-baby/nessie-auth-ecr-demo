#!/bin/bash

# Check if required environment variables are set
if [ -z "$LOCKNESSIE_OPENID_CLIENT_ID" ]; then
    echo "Error: LOCKNESSIE_OPENID_CLIENT_ID is not set"
    exit 1
fi

if [ -z "$LOCKNESSIE_OPENID_TENANT" ]; then
    echo "Error: LOCKNESSIE_OPENID_TENANT is not set"
    exit 1
fi

if [ -z "$LOCKNESSIE_OPENID_SECRET" ]; then
    echo "Error: LOCKNESSIE_OPENID_SECRET is not set"
    exit 1
fi

if [ -z "$TOKEN" ]; then
    echo "Error: TOKEN is not set"
    exit 1
fi

# Construct the JWKS URL
JWKS_URL="https://login.microsoftonline.com/${LOCKNESSIE_OPENID_TENANT}/discovery/v2.0/keys"

# Run the Java JWT validator
java -jar /app/jwt-validator.jar "$TOKEN" "$LOCKNESSIE_OPENID_CLIENT_ID" "$JWKS_URL" "$LOCKNESSIE_OPENID_SECRET"
#!/bin/sh

if [ -z "$TOKEN" ] || [ -z "$LOCKNESSIE_OPENID_CLIENT_ID" ] || [ -z "$LOCKNESSIE_OPENID_TENANT" ] || [ -z "$LOCKNESSIE_OPENID_SECRET" ]; then
    echo "Error: Required environment variables are not set"
    echo "TOKEN: $TOKEN"
    echo "CLIENT_ID: $LOCKNESSIE_OPENID_CLIENT_ID"
    echo "TENANT: $LOCKNESSIE_OPENID_TENANT"
    echo "SECRET: $LOCKNESSIE_OPENID_SECRET"
    exit 1
fi

JWKS_URL="https://login.microsoftonline.com/${LOCKNESSIE_OPENID_TENANT}/discovery/keys"

java -cp jwt-validator.jar com.example.JwtValidator "$TOKEN" "$LOCKNESSIE_OPENID_CLIENT_ID" "$JWKS_URL" "$LOCKNESSIE_OPENID_SECRET"
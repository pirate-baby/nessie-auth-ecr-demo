package com.example;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.jwt.consumer.ErrorCodeValidator;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class JwtValidator {
    private final String clientId;
    private final String jwksUrl;
    private final String clientSecret;

    public JwtValidator(String clientId, String jwksUrl, String clientSecret) {
        this.clientId = clientId;
        this.jwksUrl = jwksUrl;
        this.clientSecret = clientSecret;
        System.out.println("Initialized with client ID: " + clientId);
        System.out.println("JWKS URL: " + jwksUrl);
    }

    public JwtClaims validateToken(String token) throws InvalidJwtException, MalformedClaimException, JoseException, IOException {
        System.out.println("Starting token validation...");

        // Create a JWKS resolver that will fetch the keys from the JWKS endpoint
        HttpsJwks httpsJwks = new HttpsJwks(jwksUrl);
        System.out.println("Fetching keys from JWKS endpoint...");
        List<JsonWebKey> keys = httpsJwks.getJsonWebKeys();
        System.out.println("Found " + keys.size() + " keys in JWKS");
        for (JsonWebKey key : keys) {
            System.out.println("Key ID: " + key.getKeyId());
            System.out.println("Key Type: " + key.getKeyType());
            System.out.println("Key Use: " + key.getUse());
            System.out.println("Algorithm: " + key.getAlgorithm());
            System.out.println("---");
        }

        HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJwks);

        // Create a JWT consumer that will validate the token
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKeyResolver(httpsJwksKeyResolver)
                .setExpectedAudience(clientId)  // Verify the audience claim
                .setRequireExpirationTime()     // Require expiration time
                .setRequireIssuedAt()           // Require issued at time
                .setSkipDefaultAudienceValidation() // We'll handle audience validation manually
                .build();

        try {
            // Process the token and validate it
            JwtContext jwtContext = jwtConsumer.process(token);
            JwtClaims claims = jwtContext.getJwtClaims();

            // Manual audience validation
            List<String> audiences = claims.getAudience();
            System.out.println("Token audiences: " + audiences);
            if (!audiences.contains(clientId)) {
                List<ErrorCodeValidator.Error> errors = new ArrayList<>();
                errors.add(new ErrorCodeValidator.Error(1,
                    "Token audience does not match expected client ID. Expected: " + clientId + ", Found: " + audiences));
                throw new InvalidJwtException("Invalid audience", errors, jwtContext);
            }

            System.out.println("Token validation successful");
            System.out.println("Claims: " + claims.toJson());

            return claims;
        } catch (InvalidJwtException e) {
            System.err.println("Token validation failed!");
            System.err.println("Error message: " + e.getMessage());

            List<ErrorCodeValidator.Error> errors = e.getErrorDetails();
            if (errors != null && !errors.isEmpty()) {
                System.err.println("Validation errors:");
                for (ErrorCodeValidator.Error error : errors) {
                    System.err.println("- Error code: " + error.getErrorCode());
                    System.err.println("  Error message: " + error.getErrorMessage());
                }
            }

            throw e;
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Invalid number of arguments");
            System.out.println("Usage: java com.example.JwtValidator <token> <clientId> <jwksUrl> <clientSecret>");
            System.exit(1);
        }

        String token = args[0];
        String clientId = args[1];
        String jwksUrl = args[2];
        String clientSecret = args[3];

        try {
            JwtValidator validator = new JwtValidator(clientId, jwksUrl, clientSecret);
            JwtClaims claims = validator.validateToken(token);
            System.out.println("Token is valid!");
        } catch (InvalidJwtException e) {
            System.err.println("Invalid JWT: " + e.getMessage());
            System.exit(1);
        } catch (MalformedClaimException e) {
            System.err.println("Malformed claim: " + e.getMessage());
            System.exit(1);
        } catch (JoseException e) {
            System.err.println("Error fetching JWKS: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error connecting to JWKS endpoint: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

package com.example;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

public class JwtValidator {
    private final String clientId;
    private final String jwksUrl;
    private final String clientSecret;

    public JwtValidator(String clientId, String jwksUrl, String clientSecret) {
        this.clientId = clientId;
        this.jwksUrl = jwksUrl;
        this.clientSecret = clientSecret;
    }

    public JwtClaims validateToken(String token) throws InvalidJwtException, MalformedClaimException {
        // Create a JWKS resolver that will fetch the keys from the JWKS endpoint
        HttpsJwks httpsJwks = new HttpsJwks(jwksUrl);
        HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJwks);

        // Create a JWT consumer that will validate the token
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKeyResolver(httpsJwksKeyResolver)
                .setExpectedAudience(clientId)  // Verify the audience claim
                .setRequireExpirationTime()     // Require expiration time
                .setRequireIssuedAt()           // Require issued at time
                .build();

        // Process the token and validate it
        return jwtConsumer.processToClaims(token);
    }

    public static void main(String[] args) {
        if (args.length != 4) {
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
            System.out.println("Claims: " + claims.toJson());
        } catch (InvalidJwtException e) {
            System.out.println("Invalid JWT: " + e.getMessage());
        } catch (MalformedClaimException e) {
            System.out.println("Malformed claim: " + e.getMessage());
        }
    }
}
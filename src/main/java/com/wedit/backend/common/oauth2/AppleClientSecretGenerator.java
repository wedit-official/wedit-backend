package com.wedit.backend.common.oauth2;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@ConditionalOnExpression(
        "!'${app.oauth2.apple.team-id:}'.isEmpty() && " +
        "!'${app.oauth2.apple.key-id:}'.isEmpty() && " +
        "!'${spring.security.oauth2.client.registration.apple.client-id:}'.isEmpty() && " +
        "!'${app.oauth2.apple.private-key:}'.isEmpty()"
)
public class AppleClientSecretGenerator {

    private final String teamId;
    private final String keyId;
    private final String clientId;
    private final String privateKeyPem;

    public AppleClientSecretGenerator(
            @Value("${app.oauth2.apple.team-id}") String teamId,
            @Value("${app.oauth2.apple.key-id}") String keyId,
            @Value("${spring.security.oauth2.client.registration.apple.client-id}") String clientId,
            @Value("${app.oauth2.apple.private-key}") String privateKeyPem) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.clientId = clientId;
        this.privateKeyPem = privateKeyPem;
    }

    public String generate() {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(300);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .audience("https://appleid.apple.com")
                    .subject(clientId)
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(keyId)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new ECDSASigner(loadPrivateKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate Apple client secret", e);
        }
    }

    private ECPrivateKey loadPrivateKey() {
        try {
            String cleaned = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(cleaned);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PrivateKey key = kf.generatePrivate(spec);
            return (ECPrivateKey) key;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Apple private key", e);
        }
    }
}

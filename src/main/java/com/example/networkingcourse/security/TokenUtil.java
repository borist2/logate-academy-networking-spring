package com.example.networkingcourse.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenUtil
{
    private static final String CLAIM_KEY_AUTHORITIES = "authorities";

    private final JWSVerifier jwsVerifier;
    private final JWSSigner jwsSigner;

    private final JWTProperties jwtProperties;

    public TokenUtil(JWTProperties jwtProperties) throws JOSEException
    {
        this.jwtProperties = jwtProperties;

        jwsVerifier = new MACVerifier(jwtProperties.secret());
        jwsSigner = new MACSigner(jwtProperties.secret());
    }

    public boolean validateToken(String authToken)
    {
        try
        {
            var signedJWT = SignedJWT.parse(authToken);

            var issuer = getIssuer(signedJWT);
            if (signedJWT.getHeader().getAlgorithm().equals(Algorithm.NONE) ||
                    !StringUtils.equals(issuer, "networking")
            )
            {
                // If algorithm is NONE, not valid token.
                log.info("Token algorithm is NONE or does not have issuer.");
                return false;
            }

            return signedJWT.verify(jwsVerifier);

        } catch (ParseException e)
        {
            log.info("Exception parsing token [{}]. Error message: [{}]", authToken, e.getMessage());
            log.trace("Full error:", e);

        } catch (JOSEException e)
        {
            log.info("Exception verifying token [{}]. Error message: [{}]", authToken, e.getMessage());
            log.trace("Full error:", e);
        } catch (Exception ex)
        {
            log.error("Exception verifing token [{}]", ex.getMessage());
            log.debug("Full error: ", ex);
        }

        return false;
    }

    private String getIssuer(SignedJWT signedJWT) throws ParseException
    {
        if (signedJWT == null) return null;
        if (signedJWT.getJWTClaimsSet() == null) return null;
        return signedJWT.getJWTClaimsSet().getIssuer();
    }

    public Authentication getAuthentication(String authToken)
    {
        try
        {
            var signedJWT = SignedJWT.parse(authToken);

            var claims = signedJWT.getJWTClaimsSet();

            String username = claims.getSubject();

            log.trace("Logging in: [{}]", username);

            Collection<? extends GrantedAuthority> authorities;

            var authoritiesClaim = claims.getStringClaim(CLAIM_KEY_AUTHORITIES);
            if (io.micrometer.common.util.StringUtils.isNotBlank(authoritiesClaim))
            {
                authorities = Arrays.stream(authoritiesClaim.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();
            } else
            {
                authorities = Collections.emptyList();
            }

            return new UsernamePasswordAuthenticationToken(username, authToken, authorities);
        } catch (ParseException e)
        {
            log.error("Unable to parse token [{}]. Error message [{}]", authToken, e.getMessage());
            log.trace("Full error:", e);
            throw new BadCredentialsException("Invalid token.");
        }
    }

    public String createToken(Authentication authentication) throws JOSEException
    {
        String authorities = prepareAuthoritiesFromAuthentication(authentication);

        Date validity = generateExpirationDate();

        var claims = new JWTClaimsSet.Builder()
                .subject(authentication.getName())
                .claim(CLAIM_KEY_AUTHORITIES, authorities)
                .expirationTime(validity)
                .issueTime(Date.from(Instant.now()))
                .issuer("networking")
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

        signedJWT.sign(jwsSigner);

        return signedJWT.serialize();
    }

    private String prepareAuthoritiesFromAuthentication(Authentication authentication)
    {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private Date generateExpirationDate()
    {
        return Date.from(Instant.now().plus(jwtProperties.expiration(), ChronoUnit.SECONDS));
    }
}

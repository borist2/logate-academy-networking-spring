package com.example.networkingcourse.controllers;

import com.example.networkingcourse.model.AuthenticationRequest;
import com.example.networkingcourse.model.AuthenticationResponse;
import com.example.networkingcourse.security.TokenUtil;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
@RequiredArgsConstructor
@Slf4j
public class AuthenticateController
{
    private final AuthenticationManager authenticationManager;
    private final TokenUtil tokenUtil;

    @PostMapping
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request)
    {
        Authentication authentication = new UsernamePasswordAuthenticationToken(request.username(), request.password());

        Authentication successfulAuthentication = authenticationManager.authenticate(authentication);

        String token;
        try
        {
            token = tokenUtil.createToken(successfulAuthentication);
        } catch (JOSEException e)
        {
            log.error("There was error during authentication [{}]", e.getMessage());
            return ResponseEntity.internalServerError().build(); // Return error message
        }

        return ResponseEntity.ok(new AuthenticationResponse(token));
    }
}

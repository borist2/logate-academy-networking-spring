package com.example.networkingcourse.model;

import java.io.Serializable;

public class AuthenticationResponse implements Serializable
{
    private static final long serialVersionUID = -889842567098802629L;

    private String token;

    public AuthenticationResponse()
    {
    }

    public AuthenticationResponse(String token)
    {
        this.token = token;
    }

    public String getToken()
    {
        return this.token;
    }
}

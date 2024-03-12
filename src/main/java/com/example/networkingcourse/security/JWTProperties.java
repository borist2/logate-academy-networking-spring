package com.example.networkingcourse.security;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties("jwt")
public record JWTProperties(@NotBlank String secret,
                            @DefaultValue("64800") long expiration,
                            @DefaultValue @NotNull UserProperties user)
{

    public record UserProperties(@DefaultValue("false") boolean verifyInDatabase,
                                 @DefaultValue("false") boolean verifyUsingLastPasswordResetDate)
    {
    }
}

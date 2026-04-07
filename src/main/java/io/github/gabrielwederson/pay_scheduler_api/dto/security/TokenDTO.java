package io.github.gabrielwederson.pay_scheduler_api.dto.security;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class TokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private Boolean authenticated;
    private Date created;
    private Date expiration;
    private String accessToken;
    private String refreshToken;

    public TokenDTO() {
    }

    public TokenDTO(String email, Boolean authenticated, Date created, Date expiration, String accessToken, String refreshToken) {
        this.email = email;
        this.authenticated = authenticated;
        this.created = created;
        this.expiration = expiration;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return email;
    }

    public void setUsername(String email) {
        this.email = email;
    }

    public Boolean getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TokenDTO tokenDTO = (TokenDTO) o;
        return Objects.equals(email, tokenDTO.email) && Objects.equals(authenticated, tokenDTO.authenticated) && Objects.equals(created, tokenDTO.created) && Objects.equals(expiration, tokenDTO.expiration) && Objects.equals(accessToken, tokenDTO.accessToken) && Objects.equals(refreshToken, tokenDTO.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, authenticated, created, expiration, accessToken, refreshToken);
    }
}

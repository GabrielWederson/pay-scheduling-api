package io.github.gabrielwederson.pay_scheduler_api.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.TokenDTO;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidJWTAuthenticationException;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey = "secret";

    @Value("${security.jwt.token.expire-lenght:3600000}")
    private long validityInMilliseconds = 3600000;

    @Autowired
    private UserDetailsService userDetailsService;

    Algorithm algorithm = null;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        algorithm = Algorithm.HMAC256(secretKey.getBytes());
    }

    public TokenDTO createAccessToken(String email, List<String> roles) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        String accessToken = getAccessToken(email, roles, now, validity);
        String refreshToken = getRefreshToken(email, roles, now);
        return new TokenDTO(email, true, now, validity, accessToken, refreshToken);
    }

    public TokenDTO refreshToken(String refreshToken) {
        var token = "";
        if(refreshTokenContainsBearer(refreshToken)) {
            token = refreshToken.substring("Bearer ".length());
        }

        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        String email = decodedJWT.getSubject();
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
        return createAccessToken(email, roles);
    }

    private String getRefreshToken(String email, List<String> roles, Date now) {
        Date refreshTokenValidity = new Date(now.getTime() + (validityInMilliseconds * 3));
        return JWT.create()
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(refreshTokenValidity)
                .withSubject(email)
                .sign(algorithm);
    }

    private String getAccessToken(String email, List<String> roles, Date now, Date validity) {
        String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return JWT.create()
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withSubject(email)
                .withIssuer(issuerUrl)
                .sign(algorithm);
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = decodedToken(token);
        UserDetails userDetails = this.userDetailsService
                .loadUserByUsername(decodedJWT.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT decodedToken(String token) {
        Algorithm alg = Algorithm.HMAC256(secretKey.getBytes());
        JWTVerifier verifier = JWT.require(alg).build();
        return verifier.verify(token);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(refreshTokenContainsBearer(bearerToken)) return bearerToken.substring("Bearer ".length());
        return null;
    }

    private static boolean refreshTokenContainsBearer(String refreshToken) {
        return StringUtils.isNotBlank(refreshToken) && refreshToken.startsWith("Bearer ");
    }

    public boolean validToken(String token){
        DecodedJWT decodedJWT = decodedToken(token);
        try {
            return !decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            throw new InvalidJWTAuthenticationException("Expired or Invalid JWT Token!");
        }
    }
}


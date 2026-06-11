package com.gym.mall.utils;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.token.secretKey}")
    private String secretKey;

    @Value("${jwt.token.expireHours:72}")
    private long expireHours;

    public String getToken(String userId, String userName, String role) {
        JWTCreator.Builder builder = JWT.create();
        return builder.withClaim("userId", userId)
                .withClaim("userName", userName)
                .withClaim("role", role)
                .withClaim("timeStamp", Instant.now().toEpochMilli())
                .withExpiresAt(Instant.now().plus(expireHours, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public Map<String, String> parseToken(String token) {
        HashMap<String, String> map = new HashMap<>();
        DecodedJWT decodedJwt = JWT.require(Algorithm.HMAC256(secretKey))
                .build().verify(token);

        map.put("userId", decodedJwt.getClaim("userId").asString());
        map.put("userName", decodedJwt.getClaim("userName").asString());
        map.put("role", decodedJwt.getClaim("role").asString());
        map.put("timeStamp", decodedJwt.getClaim("timeStamp").asLong().toString());
        return map;
    }
}

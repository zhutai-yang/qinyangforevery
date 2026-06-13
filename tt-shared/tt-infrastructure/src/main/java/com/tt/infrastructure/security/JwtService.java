package com.tt.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expireMs;

  public JwtService(
      @Value("${tt.jwt.secret}") String secret,
      @Value("${tt.jwt.expire-hours:168}") long expireHours) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] bytes = md.digest(secret.getBytes(StandardCharsets.UTF_8));
      this.key = Keys.hmacShaKeyFor(bytes);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    this.expireMs = expireHours * 3600_000L;
  }

  public String createToken(long userId, String username) {
    Date now = new Date();
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("username", username)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expireMs))
        .signWith(key)
        .compact();
  }

  public Long parseUserId(String token) {
    Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return Long.parseLong(c.getSubject());
  }

  public boolean validate(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}

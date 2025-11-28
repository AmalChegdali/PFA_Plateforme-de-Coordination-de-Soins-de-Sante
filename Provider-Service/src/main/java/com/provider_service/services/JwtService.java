package com.provider_service.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.provider_service.models.Provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	 @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
	    private String secretKey;

	    @Value("${jwt.expiration:86400000}") // 24 hours
	    private long jwtExpiration;

	    public String extractUsername(String token) {
	        return extractClaim(token, Claims::getSubject);
	    }

	    // ✅ NEW: Extract role from token
	    public String extractRole(String token) {
	        return extractClaim(token, claims -> claims.get("role", String.class));
	    }

	    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
	        final Claims claims = extractAllClaims(token);
	        return claimsResolver.apply(claims);
	    }

	    public String generateToken(UserDetails userDetails) {
	        return generateToken(new HashMap<>(), userDetails);
	    }

	    // ✅ UPDATED: Add role to extra claims
	    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
	        // Add role to claims
	        if (userDetails instanceof Provider) 
	            extraClaims.put("role", ((Provider) userDetails).getRole().name());
	        
	        return buildToken(extraClaims, userDetails, jwtExpiration);
	    }

	    private String buildToken(
	            Map<String, Object> extraClaims,
	            UserDetails userDetails,
	            long expiration
	    ) {
	        return Jwts
	                .builder()
	                .setClaims(extraClaims)
	                .setSubject(userDetails.getUsername())
	                .setIssuedAt(new Date(System.currentTimeMillis()))
	                .setExpiration(new Date(System.currentTimeMillis() + expiration))
	                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
	                .compact();
	    }

	    public boolean isTokenValid(String token, UserDetails userDetails) {
	        final String username = extractUsername(token);
	        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	    }

	    private boolean isTokenExpired(String token) {
	        return extractExpiration(token).before(new Date());
	    }

	    private Date extractExpiration(String token) {
	        return extractClaim(token, Claims::getExpiration);
	    }

	    private Claims extractAllClaims(String token) {
	        return Jwts
	                .parserBuilder()
	                .setSigningKey(getSignInKey())
	                .build()
	                .parseClaimsJws(token)
	                .getBody();
	    }

	    private Key getSignInKey() {
	        byte[] keyBytes = secretKey.getBytes();
	        return Keys.hmacShaKeyFor(keyBytes);
	    }
}

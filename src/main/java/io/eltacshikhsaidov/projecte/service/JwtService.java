package io.eltacshikhsaidov.projecte.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);
    Claims extractAllClaims(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
}
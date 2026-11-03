package com.bequian.gamerzone.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los tokens JWT.
 * Estructura del token: HEADER.PAYLOAD.SIGNATURE (firma HMAC-SHA256 con la clave secreta).
 */
@Service
public class JwtService {

    private static final String EMISOR = "gamerzone";

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${gamerzone.jwt.secret}") String secretoBase64,
                      @Value("${gamerzone.jwt.expiracion-minutos}") long expiracionMinutos) {
        // La clave debe tener al menos 256 bits (32 bytes) para HS256; si es más corta, Keys lanza un error
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(UserDetails usuario) {
        Instant ahora = Instant.now();
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(usuario.getUsername())          // a quién pertenece el token
                .claim("roles", roles)                    // dato informativo para el cliente
                .issuer(EMISOR)                           // quién lo emitió
                .issuedAt(Date.from(ahora))               // cuándo se emitió
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)                          // firma: si alguien modifica el token, deja de ser válido
                .compact();
    }

    /**
     * Devuelve el usuario del token.
     * Lanza JwtException si la firma no es válida, si expiró o si no lo emitió GamerZone.
     */
    public String extraerUsername(String token) {
        return leerClaims(token).getSubject();
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(EMISOR)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
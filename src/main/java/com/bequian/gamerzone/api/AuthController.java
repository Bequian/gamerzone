package com.bequian.gamerzone.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bequian.gamerzone.api.dto.ErrorResponse;
import com.bequian.gamerzone.api.dto.LoginRequest;
import com.bequian.gamerzone.api.dto.LoginResponse;
import com.bequian.gamerzone.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // [PÚBLICA] Recibe usuario y contraseña; si son correctos devuelve un token JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));

            UserDetails usuario = (UserDetails) autenticacion.getPrincipal();
            List<String> roles = usuario.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return ResponseEntity.ok(new LoginResponse(
                    jwtService.generarToken(usuario),
                    "Bearer",
                    jwtService.getExpiracionSegundos(),
                    usuario.getUsername(),
                    roles));

        } catch (AuthenticationException e) {
            // Mensaje genérico: no revela si falló el usuario o la contraseña
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Usuario o contraseña incorrectos"));
        }
    }
}
package org.example.backendcrcoach.security.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.backendcrcoach.domain.dto.UserRequestDTO;
import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.security.dto.AuthResponse;
import org.example.backendcrcoach.security.dto.SupercellAuthDTO;
import org.example.backendcrcoach.security.dto.UserLoginDTO;
import org.example.backendcrcoach.security.jwt.JwtUtil;
import org.example.backendcrcoach.security.user.CustomUserDetails;
import org.example.backendcrcoach.services.SupercellService;
import org.example.backendcrcoach.services.TokenBlacklistService;
import org.example.backendcrcoach.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SupercellService supercellService;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService, TokenBlacklistService tokenBlacklistService, SupercellService supercellService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.supercellService = supercellService;
    }

    @PostMapping("/authenticate")
    public AuthResponse authenticate(@RequestBody UserLoginDTO request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60 * 10);

        // Agregar la cookie a la respuesta
        response.addCookie(jwtCookie);
        return new AuthResponse(token);
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Manejar el token desde el encabezado Authorization
        String authorizationHeader = request.getHeader("Authorization");
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        // Manejar el token desde la cookie
        if (jwt == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                }
            }
        }

        // Si se encuentra el token, añadirlo a la blacklist
        if (jwt != null) {
            tokenBlacklistService.addTokenToBlacklist(jwt);

            // Eliminar la cookie
            Cookie jwtCookie = new Cookie("jwt", null);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(0); // Caducar inmediatamente
            response.addCookie(jwtCookie);

            return ResponseEntity.ok("Logout exitoso. Token añadido a la blacklist y cookie eliminada.");
        }

        return ResponseEntity.badRequest().body("No se encontró un token válido para cerrar sesión.");
    }

    // Registro de usuario
    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid UserRequestDTO dto) {
        // Registrar usuario usando el servicio
        User newUser = userService.createUser(dto);

        // Generar token JWT para el nuevo usuario
        String token = jwtUtil.generateToken(new CustomUserDetails(newUser));

        // Devolver el token en la respuesta
        return new AuthResponse(token);
    }

    @PostMapping("/supercell")
    public AuthResponse authenticateWithSupercell(@RequestBody @Valid SupercellAuthDTO dto) {
        if (dto.getPlayerTag() == null || dto.getPlayerTag().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "playerTag es obligatorio");
        }

        Map<String, Object> playerData;
        try {
            playerData = supercellService.getPlayerInfoByTag(dto.getPlayerTag(), dto.getSupercellToken());
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "No se pudo verificar el token de Supercell: " + e.getMessage());
        }

        User user = supercellService.findOrCreateUserFromSupercellData(playerData);
        String token = jwtUtil.generateToken(new org.example.backendcrcoach.security.user.CustomUserDetails(user));
        return new AuthResponse(token);
    }
}

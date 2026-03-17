package org.example.backendcrcoach.services.principal_system_services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.principal_system_dtos.user.UserRequestDTO;
import org.example.backendcrcoach.domain.dto.principal_system_dtos.user.UserResponseDTO;
import org.example.backendcrcoach.domain.entities.principal_system_entities.User;
import org.example.backendcrcoach.mappers.principal_system_mappers.UserMapper;
import org.example.backendcrcoach.repositories.principal_system_repositories.UserRepository;
import org.example.backendcrcoach.services.security_services.FileService;
import org.example.backendcrcoach.services.communication_services.EmailService;
import org.example.backendcrcoach.web.exceptions.other_exceptions.ResourceNotFoundException;
import org.example.backendcrcoach.web.exceptions.principal_system_exceptions.user.DuplicatedUserException;
import org.example.backendcrcoach.web.exceptions.principal_system_exceptions.user.UserNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@Transactional
@EnableScheduling
public class UserService {
    public static final String USUARIO_NO_ENCONTRADO_CON = "Usuario no encontrado con ";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmailService emailService;
    private final PlayerProfileService playerProfileService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService, EmailService emailService, PlayerProfileService playerProfileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.emailService = emailService;
        this.playerProfileService = playerProfileService;
    }

    public Page<UserResponseDTO> list(Pageable pageable) {
        Page<UserResponseDTO> users = userRepository.findAll(pageable).map(UserMapper::toDTO);
        return users;
    }

    public UserResponseDTO showById(Long id) {
        User user = userRepository.getUsersById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        } else {
            return UserMapper.toDTO(user);
        }
    }

    public UserResponseDTO showByName(String username) {
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        } else {
            return UserMapper.toDTO(user);
        }
    }

    public UserResponseDTO showByEmail(String email) {
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(email);
        } else {
            return UserMapper.toDTO(user);
        }
    }

    public UserResponseDTO create(UserRequestDTO dto) {
        String username = dto.getUsername() != null ? dto.getUsername().toLowerCase() : null;
        String email = dto.getEmail() != null ? dto.getEmail().toLowerCase() : null;

        if (username != null && userRepository.existsByUsername(username)) {
            throw new DuplicatedUserException(username);
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new DuplicatedUserException(email);
        }

        User user = UserMapper.toEntity(dto);
        if (user.getUsername() != null) user.setUsername(user.getUsername().toLowerCase());
        if (user.getEmail() != null) user.setEmail(user.getEmail().toLowerCase());
        if (dto.getPasswordHash() != null) user.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));

        User savedUser = userRepository.save(user);

        // Enviar correo de bienvenida/registro
        try {
            String subject = "Bienvenido a MemoWorks";
            Map<String, Object> model = new HashMap<>();
            model.put("user", savedUser);
            emailService.sendTemplateEmail(savedUser.getEmail(), subject, "saludo.html", model);
        } catch (Exception e) {
            // fallback simple
            try {
                String subject = "Bienvenido a MemoWorks";
                String text = "Gracias por registrarte en MemoWorks.";
                emailService.sendSimpleEmail(savedUser.getEmail(), subject, text);
            } catch (Exception ex) {
            }
        }

        return UserMapper.toDTO(savedUser);
    }

    public UserResponseDTO update(Long id, @RequestBody UserRequestDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        // validar unicidad de username y email excluyendo al propio usuario
        if (dto.getUsername() != null && userRepository.existsByUsernameAndIdNot(dto.getUsername(), id)) {
            throw new DuplicatedUserException(dto.getUsername());
        }
        if (dto.getEmail() != null && userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new DuplicatedUserException(dto.getEmail());
        }

        updateBasicFields(dto, user);

        // si se provee contraseña, codificar antes de guardar
        if (dto.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
        }

        User updatedUser = userRepository.save(user);
        return UserMapper.toDTO(updatedUser);
    }

    private void updateBasicFields(UserRequestDTO user, User updatedUser) {
        Optional.ofNullable(user.getUsername()).ifPresent(updatedUser::setUsername);
        Optional.ofNullable(user.getEmail()).ifPresent(updatedUser::setEmail);
        Optional.ofNullable(user.getPasswordHash()).ifPresent(updatedUser::setPasswordHash);
        Optional.ofNullable(user.getAvatarUrl()).ifPresent(updatedUser::setAvatarUrl);
        Optional.ofNullable(user.getRole()).ifPresent(updatedUser::setRole);
        Optional.ofNullable(user.getCreatedAt()).ifPresent(updatedUser::setCreatedAt);
        Optional.ofNullable(user.getPlayerTag()).ifPresent(updatedUser::setPlayerTag);
        Optional.ofNullable(user.getEnabled()).ifPresent(updatedUser::setEnabled);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) throw new IllegalArgumentException("User not found");
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User createUser(UserRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicatedUserException(dto.getUsername());
        } else {
            User user = UserMapper.toEntity(dto);
            user.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
            User savedUser = userRepository.save(user);
            // enviar correo de registro
            try {
                String subject = "Bienvenido a CRCoach";
                Map<String, Object> model = new HashMap<>();
                model.put("user", savedUser);
                emailService.sendTemplateEmail(savedUser.getEmail(), subject, "saludo.html", model);
            } catch (Exception e) {
                try {
                    emailService.sendSimpleEmail(savedUser.getEmail(), "Bienvenido a CRCoach", "Gracias por registrarte en CRCoach.");
                } catch (Exception ex) {
                    throw new RuntimeException("Error al enviar correo de bienvenida a " + savedUser.getEmail(), ex);
                }
            }
            return savedUser;
        }
    }

    /******************************************************************************************************/

    public User obtenerMiPerfil() {
        // Obtener contexto de autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificar si el usuario está autenticado
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Usuario no autenticado.");
        }
        String email = authentication.getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USUARIO_NO_ENCONTRADO_CON + "email " + email));
    }

    public Resource obtenerAvatarGenerico(Long id) {
        User usuario = (id == null) ? obtenerMiPerfil() : obtenerUsuarioPorId(id);
        if (usuario.getAvatarUrl() == null || usuario.getAvatarUrl().isEmpty()) {
            throw new ResourceNotFoundException("El usuario no tiene un avatar asignado.");
        }
        return fileService.cargarFichero(usuario.getAvatarUrl());
    }

    public void guardarAvatar(Long usuarioId, MultipartFile avatar) throws IOException {
        validarTamanoArchivo(avatar);
        validarTipoDeArchivo(avatar);
        User usuario = userRepository.findById(usuarioId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        String rutaArchivo = fileService.guardarFichero(usuarioId, avatar);
        usuario.setAvatarUrl(rutaArchivo);
        userRepository.save(usuario);
    }

    private void validarTamanoArchivo(MultipartFile avatar) {
        long maxSizeInBytes = 1024 * 1024 * 5L; // 5MB
        if (avatar.getSize() > maxSizeInBytes) {
            throw new IllegalArgumentException("Tamaño de archivo excede el límite de 5MB");
        }
    }

    private void validarTipoDeArchivo(MultipartFile avatar) {
        String contentType = avatar.getContentType();
        if (!Arrays.asList("image/png", "image/jpeg", "image/gif", "image/webp").contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo debe ser: (jpeg, png, gif, webp)");
        }
    }

    public User obtenerUsuarioPorId(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(USUARIO_NO_ENCONTRADO_CON + "id " + id));
    }
}

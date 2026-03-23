package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.UserRequestDTO;
import org.example.backendcrcoach.domain.dto.UserResponseDTO;
import org.example.backendcrcoach.domain.entities.PlayerProfile;
import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.mappers.UserMapper;
import org.example.backendcrcoach.repositories.PlayerProfileRepository;
import org.example.backendcrcoach.repositories.UserRepository;
import org.example.backendcrcoach.web.exceptions.ResourceNotFoundException;
import org.example.backendcrcoach.web.exceptions.DuplicatedUserException;
import org.example.backendcrcoach.web.exceptions.UserNotFoundException;
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
public class UserService {
    public static final String USUARIO_NO_ENCONTRADO_CON = "Usuario no encontrado con ";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final EmailService emailService;
    private final PlayerProfileService playerProfileService;
    private final PlayerProfileRepository playerProfileRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FileService fileService,
                       EmailService emailService,
                       PlayerProfileService playerProfileService,
                       PlayerProfileRepository playerProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.emailService = emailService;
        this.playerProfileService = playerProfileService;
        this.playerProfileRepository = playerProfileRepository;
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
        user.setPlayerTag(null);
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
        Optional.ofNullable(user.getName()).ifPresent(updatedUser::setName);
        Optional.ofNullable(user.getSurnames()).ifPresent(updatedUser::setSurnames);
        Optional.ofNullable(user.getUsername()).ifPresent(updatedUser::setUsername);
        Optional.ofNullable(user.getEmail()).ifPresent(updatedUser::setEmail);
        Optional.ofNullable(user.getPasswordHash()).ifPresent(updatedUser::setPasswordHash);
        Optional.ofNullable(user.getAvatarUrl()).ifPresent(updatedUser::setAvatarUrl);
        Optional.ofNullable(user.getRole()).ifPresent(updatedUser::setRole);
        Optional.ofNullable(user.getCreatedAt()).ifPresent(updatedUser::setCreatedAt);
        // El perfil de Clash se vincula en un endpoint dedicado tras registro.
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
            user.setPlayerTag(null);
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

    /**
     * Vincula un tag de Clash Royale al perfil del usuario autenticado.
     * 
     * Esta función:
     * 1. Obtiene el usuario autenticado actual
     * 2. Verifica que el tag no esté ya vinculado a otra cuenta
     * 3. Busca el perfil en la BD, si no existe lo obtiene de la API de Supercell
     * 4. Vincula correctamente el tag y el perfil al usuario
     * 5. Guarda los cambios en la BD
     * 
     * @param tag El tag del jugador (con o sin #)
     * @throws IllegalArgumentException si el tag es inválido, ya existe en otra cuenta, o no existe en la API
     */
    public void bindPlayerTagToCurrentUser(String tag) {
        // 1) Obtener el usuario autenticado actual
        User usuario = obtenerMiPerfil();
        String normalizedTag = normalizeTag(tag);

        // 2) Verificar que el tag no esté ya vinculado a otra cuenta
        if (userRepository.existsByPlayerTagAndIdNot(normalizedTag, usuario.getId())) {
            throw new IllegalArgumentException("El playerTag ya está vinculado a otra cuenta.");
        }

        // 3) Obtener o crear el perfil del jugador
        PlayerProfile profile = obtenerOCrearPerfilJugador(normalizedTag);

        // 4) Vincular el tag al usuario
        usuario.setPlayerTag(profile.getTag());
        // IMPORTANTE: No usar setPlayerProfile aquí porque la relación es OneToOne(fetch=LAZY)
        // y está configurada como insertable=false, updatable=false en la BD
        // El tag es la clave que vincula ambas entidades
        
        // 5) Guardar los cambios
        userRepository.save(usuario);
    }

    /**
     * Desvincula el tag de Clash Royale de un usuario específico por su ID.
     * 
     * @param id ID del usuario al que desvinculari el tag
     * @throws ResourceNotFoundException si el usuario no existe
     * @throws IllegalArgumentException si el usuario no tiene tag vinculado
     */
    public void unbindPlayerTagFromUser(Long id) {
        User usuario = obtenerUsuarioPorId(id);
        
        if (usuario.getPlayerTag() == null || usuario.getPlayerTag().isBlank()) {
            throw new IllegalArgumentException("El usuario no tiene ningún playerTag vinculado.");
        }

        usuario.setPlayerTag(null);
        userRepository.save(usuario);
    }

    /**
     * Obtiene el perfil del jugador desde la BD o de la API de Supercell.
     * 
     * @param normalizedTag El tag normalizado del jugador (con #)
     * @return PlayerProfile El perfil del jugador
     * @throws IllegalArgumentException si el perfil no existe en la BD ni en la API
     */
    private PlayerProfile obtenerOCrearPerfilJugador(String normalizedTag) {
        // Buscar en la BD primero
        Optional<PlayerProfile> profileOpt = playerProfileRepository.findByTag(normalizedTag);
        
        if (profileOpt.isPresent()) {
            return profileOpt.get();
        }

        // Si no existe en BD, intentar obtenerlo de la API de Supercell
        // El método getPlayer() se encarga de:
        // - Llamar a la API de Supercell
        // - Mapear la respuesta a PlayerProfile
        // - Guardar o actualizar el perfil en la BD
        // - Guardar snapshot e importar batallas
        playerProfileService.getPlayer(normalizedTag.substring(1)); // Remover # para la API

        // Ahora buscar en BD después de guardar desde API
        return playerProfileRepository.findByTag(normalizedTag)
                .orElseThrow(() -> new IllegalArgumentException("No se pudo obtener ni almacenar el perfil del jugador con tag: " + normalizedTag));
    }

    private String normalizeTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("El playerTag no puede estar vacío.");
        }
        String normalized = tag.trim().toUpperCase();
        return normalized.startsWith("#") ? normalized : "#" + normalized;
    }
}

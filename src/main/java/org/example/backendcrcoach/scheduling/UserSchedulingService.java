package org.example.backendcrcoach.scheduling;

import org.example.backendcrcoach.domain.entities.User;
import org.example.backendcrcoach.services.UserService;
import org.example.backendcrcoach.services.PlayerProfileService;
import org.example.backendcrcoach.security.user.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class UserSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(UserSchedulingService.class);

    private final ThreadPoolTaskScheduler scheduler;
    private final UserService userService;
    private final PlayerProfileService playerProfileService;
    private final boolean autoStart;
    private final boolean schedulingEnabled;

    // Map userId -> future
    private final Map<Long, ScheduledFuture<?>> userTasks = new ConcurrentHashMap<>();

    public UserSchedulingService(ThreadPoolTaskScheduler scheduler, UserService userService, PlayerProfileService playerProfileService,
                                 @Value("${scheduling.autoStart:false}") boolean autoStart,
                                 @Value("${scheduling.enabled:false}") boolean schedulingEnabled) {
        this.scheduler = scheduler;
        this.userService = userService;
        this.playerProfileService = playerProfileService;
        this.autoStart = autoStart;
        this.schedulingEnabled = schedulingEnabled;
    }

    /**
     * Inicia la tarea programada para el usuario si tiene playerTag vinculado.
     * No inicia si ya existe una tarea para el usuario.
     */
    public void startForCurrentUser(Long userId, long fixedDelayMs) {
        if (!schedulingEnabled) {
            log.debug("Scheduling is disabled by configuration (scheduling.enabled=false)");
            return;
        }

        if (!autoStart) {
            log.debug("Auto-start of user scheduling is disabled (scheduling.autoStart=false)");
            return;
        }

        if (userTasks.containsKey(userId)) return;

        // Si existe un SecurityContext, comprobar que corresponde al usuario solicitado.
        // Si no existe (por ejemplo durante el flujo de authenticate donde el controller tiene
        // la Authentication localmente), permitimos iniciar la tarea confiando en el caller.
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
            if (!userId.equals(details.getId())) {
                log.debug("Authenticated user id {} does not match requested userId {} - skipping scheduling", details.getId(), userId);
                return;
            }
        } else {
            log.debug("No SecurityContext available when starting scheduling for user {} - proceeding because caller provided the userId", userId);
        }

        // Comprobar inicialmente que el usuario tiene tag vinculado
        User user = userService.obtenerUsuarioPorId(userId);
        if (user.getPlayerTag() == null || user.getPlayerTag().isBlank()) {
            log.debug("User {} has no playerTag, skipping scheduling", userId);
            return;
        }

        // En el Runnable recuperamos el usuario en cada ejecución para evitar usar
        // una entidad potencialmente obsoleta y para recoger cambios realizados
        // sobre el playerTag sin necesidad de reiniciar la tarea.
        Runnable task = () -> {
            try {
                User current = userService.obtenerUsuarioPorId(userId);
                if (current == null) {
                    log.warn("Scheduled task: user {} not found, cancelling task", userId);
                    stopForCurrentUser(userId);
                    return;
                }
                String tag = current.getPlayerTag();
                if (tag == null || tag.isBlank()) {
                    log.debug("Scheduled task: user {} has no playerTag, skipping this iteration", userId);
                    return;
                }
                String rawTag = tag.startsWith("#") ? tag.substring(1) : tag;
                playerProfileService.getPlayer(rawTag);
            } catch (Exception e) {
                log.error("Error in scheduled task for user {}: {}", userId, e.getMessage(), e);
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, fixedDelayMs);

        userTasks.put(userId, future);
        log.info("Started scheduled sync for user {}", userId);
    }

    public boolean hasActiveTasks() {
        return !userTasks.isEmpty();
    }

    public void stopForCurrentUser(Long userId) {
        ScheduledFuture<?> future = userTasks.remove(userId);
        if (future != null) {
            future.cancel(true);
            log.info("Stopped scheduled sync for user {}", userId);
        }
    }
}


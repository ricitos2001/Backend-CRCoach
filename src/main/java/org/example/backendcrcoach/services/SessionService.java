package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.SessionRequestDTO;
import org.example.backendcrcoach.domain.dto.SessionResponseDTO;
import org.example.backendcrcoach.domain.entities.Session;
import org.example.backendcrcoach.mappers.SessionMapper;
import org.example.backendcrcoach.repositories.SessionRepository;
import org.example.backendcrcoach.web.exceptions.DuplicatedGoalException;
import org.example.backendcrcoach.web.exceptions.DuplicatedSessionException;
import org.example.backendcrcoach.web.exceptions.SessionNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionService {
    private final SessionRepository sessionRepository;
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionResponseDTO create(SessionRequestDTO dto) {
        String title = dto.getTitle() != null ? dto.getTitle().toLowerCase() : null;
        if (title != null && sessionRepository.existsByTitle(title)) {
            throw new DuplicatedGoalException(title);
        }
        Session session = SessionMapper.toEntity(dto);
        if (session.getTitle() != null) session.setTitle(session.getTitle().toLowerCase());
        Session savedSession = sessionRepository.save(session);
        return SessionMapper.toDTO(savedSession);
    }

    public Page<SessionResponseDTO> list(Pageable pageable) {
        return sessionRepository.findAll(pageable).map(SessionMapper::toDTO);
    }

    public SessionResponseDTO showByTitle(String title) {
        Session session = sessionRepository.getSessionByTitle(title);
        if (session == null) {
            throw new SessionNotFoundException(title);
        } else {
            return SessionMapper.toDTO(session);
        }
    }

    public SessionResponseDTO showById(Long id) {
        Session session = sessionRepository.getSessionById(id);
        if (session == null) {
            throw new SessionNotFoundException(id);
        } else {
            return SessionMapper.toDTO(session);
        }
    }

    public SessionResponseDTO update(Long id, @RequestBody SessionRequestDTO dto) {
        Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
        if (dto.getTitle() != null && sessionRepository.existsByTitleAndIdNot(dto.getTitle(), id)) {
            throw new DuplicatedSessionException(dto.getTitle());
        }
        updateBasicFields(dto, session);
        Session updatedSession = sessionRepository.save(session);
        return SessionMapper.toDTO(updatedSession);
    }

    private void updateBasicFields(SessionRequestDTO session, Session updatedSession) {
        Optional.ofNullable(session.getTitle()).ifPresent(updatedSession::setTitle);
        Optional.ofNullable(session.getNotes()).ifPresent(updatedSession::setNotes);
        Optional.ofNullable(session.getMood()).ifPresent(updatedSession::setMood);
        Optional.ofNullable(session.getStartTime()).ifPresent(updatedSession::setStartTime);
        Optional.ofNullable(session.getEndTime()).ifPresent(updatedSession::setEndTime);
        Optional.ofNullable(session.getCreatedAt()).ifPresent(updatedSession::setCreatedAt);
        Optional.ofNullable(session.getUser()).ifPresent(updatedSession::setUser);
    }

    public void delete(Long id) {
        if (!sessionRepository.existsById(id)) throw new SessionNotFoundException(id);
        sessionRepository.deleteById(id);
    }

    /**
     * Lista todas las sesiones de un usuario identificado por su email.
     */
    public List<SessionResponseDTO> listByUserEmail(String userEmail) {
        List<Session> sessions = sessionRepository.findByUserEmail(userEmail);
        return sessions.stream().map(SessionMapper::toDTO).collect(Collectors.toList());
    }
}

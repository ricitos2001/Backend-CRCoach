package org.example.backendcrcoach.mappers;

import org.example.backendcrcoach.domain.dto.SessionRequestDTO;
import org.example.backendcrcoach.domain.dto.SessionResponseDTO;
import org.example.backendcrcoach.domain.entities.Session;

public class SessionMapper {
    public static Session toEntity(SessionRequestDTO dto) {
        Session session = new Session();
        session.setTitle(dto.getTitle());
        session.setNotes(dto.getNotes());
        session.setMood(dto.getMood());
        session.setEnfoque(dto.getEnfoque());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setCreatedAt(dto.getCreatedAt());
        session.setUser(dto.getUser());
        return session;
    }

    public static SessionResponseDTO toDTO(Session session) {
        return new SessionResponseDTO(
                session.getId(),
                session.getTitle(),
                session.getNotes(),
                session.getMood(),
                session.getEnfoque(),
                session.getStartTime().toString(),
                session.getEndTime().toString(),
                session.getCreatedAt().toString(),
                session.getUser()
        );
    }
}

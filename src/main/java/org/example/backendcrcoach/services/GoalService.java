package org.example.backendcrcoach.services;

import jakarta.transaction.Transactional;
import org.example.backendcrcoach.domain.dto.GoalRequestDTO;
import org.example.backendcrcoach.domain.dto.GoalResponseDTO;
import org.example.backendcrcoach.domain.entities.Goal;
import org.example.backendcrcoach.mappers.GoalMapper;
import org.example.backendcrcoach.repositories.GoalRepository;
import org.example.backendcrcoach.web.exceptions.DuplicatedGoalException;
import org.example.backendcrcoach.web.exceptions.GoalNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
@Transactional
public class GoalService {
    private final GoalRepository goalRepository;
    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }
    //hacer un CRUD
    public GoalResponseDTO create(GoalRequestDTO dto) {
        String title = dto.getTitle() != null ? dto.getTitle().toLowerCase() : null;
        if (title != null && goalRepository.existsByTitle(title)) {
            throw new DuplicatedGoalException(title);
        }
        Goal goal = GoalMapper.toEntity(dto);
        if (goal.getTitle() != null) goal.setTitle(goal.getTitle().toLowerCase());
        Goal savedGoal = goalRepository.save(goal);
        return GoalMapper.toDTO(savedGoal);
    }

    public Page<GoalResponseDTO> list(Pageable pageable) {
        Page<GoalResponseDTO> groups = goalRepository.findAll(pageable).map(GoalMapper::toDTO);
        return groups;
    }

    public GoalResponseDTO showByTitle(String title) {
        Goal goal = goalRepository.getGoalByTitle(title);
        if (goal == null) {
            throw new GoalNotFoundException(title);
        } else {
            return GoalMapper.toDTO(goal);
        }
    }

    public GoalResponseDTO showById(Long id) {
        Goal goal = goalRepository.getGoalById(id);
        if (goal == null) {
            throw new GoalNotFoundException(id);
        } else {
            return GoalMapper.toDTO(goal);
        }
    }

    public GoalResponseDTO update(Long id, @RequestBody GoalRequestDTO dto) {
        Goal goal = goalRepository.findById(id).orElseThrow(() -> new GoalNotFoundException(id));
        if (dto.getTitle() != null && goalRepository.existsByTitleAndIdNot(dto.getTitle(), id)) {
            throw new DuplicatedGoalException(dto.getTitle());
        }
        updateBasicFields(dto, goal);
        Goal updatedGoal = goalRepository.save(goal);
        return GoalMapper.toDTO(updatedGoal);
    }

    private void updateBasicFields(GoalRequestDTO goal, Goal updatedGoal) {
        Optional.ofNullable(goal.getTitle()).ifPresent(updatedGoal::setTitle);
        Optional.ofNullable(goal.getDescription()).ifPresent(updatedGoal::setDescription);
        Optional.ofNullable(goal.getMetricType()).ifPresent(updatedGoal::setMetricType);
        Optional.ofNullable(goal.getTargetValue()).ifPresent(updatedGoal::setTargetValue);
        Optional.ofNullable(goal.getCurrentValue()).ifPresent(updatedGoal::setCurrentValue);
        Optional.ofNullable(goal.getStatus()).ifPresent(updatedGoal::setStatus);
        Optional.ofNullable(goal.getDeadline()).ifPresent(updatedGoal::setDeadline);
        Optional.ofNullable(goal.getCreatedAt()).ifPresent(updatedGoal::setCreatedAt);
        Optional.ofNullable(goal.getUser()).ifPresent(updatedGoal::setUser);
    }

    public void delete(Long id) {
        if (!goalRepository.existsById(id)) throw new GoalNotFoundException(id);
        goalRepository.deleteById(id);
    }
}

package com.example.sis1.service.impl;

import com.example.sis1.dto.TaskRequestDto;
import com.example.sis1.dto.TaskResponseDto;
import com.example.sis1.dto.TaskStatusUpdateDto;
import com.example.sis1.entity.Task;
import com.example.sis1.entity.TaskStatus;
import com.example.sis1.exception.ResourceConflictException;
import com.example.sis1.exception.ResourceNotFoundException;
import com.example.sis1.mapper.TaskMapper;
import com.example.sis1.repository.TaskRepository;
import com.example.sis1.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskResponseDto create(TaskRequestDto dto) {
        if (taskRepository.existsByTitleIgnoreCase(dto.getTitle())) {
            throw new ResourceConflictException(
                    "A task with title '" + dto.getTitle() + "' already exists");
        }
        Task task = taskMapper.toEntity(dto);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getById(Long id) {
        return taskMapper.toResponseDto(findTaskOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAll(TaskStatus status, Pageable pageable) {
        Page<Task> page = (status != null)
                ? taskRepository.findByStatus(status, pageable)
                : taskRepository.findAll(pageable);
        return page.map(taskMapper::toResponseDto);
    }

    @Override
    public TaskResponseDto update(Long id, TaskRequestDto dto) {
        Task task = findTaskOrThrow(id);
        if (taskRepository.existsByTitleIgnoreCaseAndIdNot(dto.getTitle(), id)) {
            throw new ResourceConflictException(
                    "A task with title '" + dto.getTitle() + "' already exists");
        }
        taskMapper.updateEntityFromDto(dto, task);
        return taskMapper.toResponseDto(taskRepository.save(task));
    }

    @Override
    public TaskResponseDto updateStatus(Long id, TaskStatusUpdateDto dto) {
        Task task = findTaskOrThrow(id);
        task.setStatus(dto.getStatus());
        return taskMapper.toResponseDto(taskRepository.save(task));
    }

    @Override
    public void delete(Long id) {
        taskRepository.delete(findTaskOrThrow(id));
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }
}

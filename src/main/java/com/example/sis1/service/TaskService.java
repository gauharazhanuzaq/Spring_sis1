package com.example.sis1.service;

import com.example.sis1.dto.TaskRequestDto;
import com.example.sis1.dto.TaskResponseDto;
import com.example.sis1.dto.TaskStatusUpdateDto;
import com.example.sis1.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponseDto create(TaskRequestDto dto);

    TaskResponseDto getById(Long id);

    Page<TaskResponseDto> getAll(TaskStatus status, Pageable pageable);

    TaskResponseDto update(Long id, TaskRequestDto dto);

    TaskResponseDto updateStatus(Long id, TaskStatusUpdateDto dto);

    void delete(Long id);
}

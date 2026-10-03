package com.example.sis1.controller;

import com.example.sis1.dto.TaskRequestDto;
import com.example.sis1.dto.TaskResponseDto;
import com.example.sis1.dto.TaskStatusUpdateDto;
import com.example.sis1.entity.TaskStatus;
import com.example.sis1.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** POST /api/tasks -> 201 Created (+ Location header), 400 on validation failure */
    @PostMapping
    public ResponseEntity<TaskResponseDto> create(@Valid @RequestBody TaskRequestDto requestDto) {
        TaskResponseDto created = taskService.create(requestDto);
        return ResponseEntity.created(URI.create("/api/tasks/" + created.getId())).body(created);
    }

    /** GET /api/tasks/{id} -> 200 OK, 404 if missing */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    /** GET /api/tasks?status=&page=&size=&sort= -> 200 OK (paginated, optionally filtered) */
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getAll(
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(taskService.getAll(status, pageable));
    }

    /** PUT /api/tasks/{id} -> 200 OK, 404 if missing, 400 on validation failure */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody TaskRequestDto requestDto) {
        return ResponseEntity.ok(taskService.update(id, requestDto));
    }

    /** PATCH /api/tasks/{id}/status -> 200 OK, 404 if missing, 400 on invalid/missing status */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateStatus(@PathVariable Long id,
                                                          @Valid @RequestBody TaskStatusUpdateDto statusDto) {
        return ResponseEntity.ok(taskService.updateStatus(id, statusDto));
    }

    /** DELETE /api/tasks/{id} -> 204 No Content, 404 if missing */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

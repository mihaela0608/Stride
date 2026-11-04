package org.example.stride.service;

import org.example.stride.model.dto.RunCreationDto;
import org.example.stride.model.dto.RunResponseDto;
import org.example.stride.model.entity.Run;

import java.util.List;

public interface RunService {
    void addRun(RunCreationDto dto);
    List<RunResponseDto> getAll();
}

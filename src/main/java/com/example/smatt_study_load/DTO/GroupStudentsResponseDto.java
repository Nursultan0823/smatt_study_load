package com.example.smatt_study_load.DTO;
import lombok.Data;
import java.util.List;

@Data
public class GroupStudentsResponseDto {
    private ResponseGroupDTO group;
    private List<StudentShortDto> students;
}
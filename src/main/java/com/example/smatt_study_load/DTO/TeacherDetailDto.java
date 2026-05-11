package com.example.smatt_study_load.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeacherDetailDto {
    private int id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String whatsApp;
    private String position;
    private boolean enabled;
    private boolean hasAvatar;
    private int disciplinesCount;
    private int materialsCount;
    private List<GetDisciplineDTO> disciplines;
    private List<TeacherScheduleDto> schedules;
    private List<UmmMaterialShortDto> recentMaterials;
}

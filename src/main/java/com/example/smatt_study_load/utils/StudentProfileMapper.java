package com.example.smatt_study_load.utils;

import java.util.List;

import com.example.smatt_study_load.DTO.GroupStudentsResponseDto;
import com.example.smatt_study_load.DTO.ResponseGroupDTO;
import com.example.smatt_study_load.DTO.StudentShortDto;
import com.example.smatt_study_load.DTO.UserShortDto;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.User;

public class StudentProfileMapper {

    public static GroupStudentsResponseDto toGroupStudentsResponseDto(GroupEntity group, List<StudentProfile> students) {
        GroupStudentsResponseDto dto = new GroupStudentsResponseDto();
        dto.setGroup(toGroupDto(group));
        dto.setStudents(
                students.stream()
                        .map(StudentProfileMapper::toStudentShortDto)
                        .toList()
        );
        return dto;
    }

    public static StudentShortDto toStudentShortDto(StudentProfile studentProfile) {
        StudentShortDto dto = new StudentShortDto();
        dto.setId(studentProfile.getId());

        if (studentProfile.getUser() != null) {
            dto.setUser(toUserDto(studentProfile.getUser()));
        }

        return dto;
    }

    public static ResponseGroupDTO toGroupDto(GroupEntity group) {
        ResponseGroupDTO dto = new ResponseGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCourseNumber(group.getCourseNumber());
        dto.setSpecialty(group.getSpecialty());

        if (isApprovedStudent(group.getStarosta())) {
            dto.setStarostaId(group.getStarosta().getId());
            dto.setStarostaName(group.getStarosta().getUser().getFullName());
        }

        return dto;
    }

    private static boolean isApprovedStudent(StudentProfile studentProfile) {
        return studentProfile != null
                && studentProfile.getUser() != null
                && studentProfile.getUser().isEnabled()
                && studentProfile.getUser().getStatus() == UserStatus.APPROVED;
    }

    public static UserShortDto toUserDto(User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}

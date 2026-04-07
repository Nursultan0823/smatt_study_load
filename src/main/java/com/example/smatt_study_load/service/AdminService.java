package com.example.smatt_study_load.service;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.DTO.AddDisciplineRequest;
import com.example.smatt_study_load.DTO.CurrentUserDto;
import com.example.smatt_study_load.DTO.GroupDTO;
import com.example.smatt_study_load.DTO.GroupStudentsResponseDto;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UpdateGroupDto;
import com.example.smatt_study_load.DTO.UserDto;
import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.StudentProfileMapper;
import com.example.smatt_study_load.utils.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final GroupEntityRepository groupEntityRepository;
    private final DisciplineRepository disciplineRepository;
     private final StudentProfileRepository studentProfileRepository;
  
    public ResponseEntity<?> AddGroupEntity(GroupDTO groupDTO){
        try{
        GroupEntity groupEntity =new GroupEntity();
        groupEntity.setName(groupDTO.getName());
        groupEntity.setCourseNumber(groupDTO.getCourseNumber());
        groupEntity.setSpecialty(groupDTO.getSpecialty());
        groupEntityRepository.save(groupEntity);
        return ResponseEntity.ok(new Response("Группа успешно добавлена"));
        }catch(Exception e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка добавления группы"));}
    }
    public ResponseEntity<?>AddDiscipline(AddDisciplineRequest request){
        try {
            Discipline discipline = new Discipline();
            discipline.setName(request.getName());
            discipline.setDescription(request.getDescription());
            disciplineRepository.save(discipline);
             return ResponseEntity.ok(new Response("Дисциплина успешно добавлена"));
        } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка добавления группы"));
        }
    }
      public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(new Response("Пользователь не авторизован"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        CurrentUserDto dto = new CurrentUserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList()
        );

        return ResponseEntity.ok(dto);
    }
    public List<UserDto>getPendingUser(){
    List<UserDto> users = userRepository.findByStatus(UserStatus.PENDING).stream()
                        .map(user -> new UserDto(
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getStatus().name(),
                                user.getRoles()
                        ))
                        .toList();
        return users;
    }
    public GroupStudentsResponseDto getStudentsByGroupId(int groupId) {
        GroupEntity group = groupEntityRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        List<StudentProfile> students = studentProfileRepository.findAllByGroupIdWithUserAndGroup(groupId);

        return StudentProfileMapper.toGroupStudentsResponseDto(group, students);
    }
     public ResponseEntity<?> updateGroup(int groupId, UpdateGroupDto dto) {
        GroupEntity group = groupEntityRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));
        try {
            if (dto.getName() != null && !dto.getName().isBlank()) {
            if (groupEntityRepository.existsByNameAndIdNot(dto.getName(), groupId)) {
                throw new RuntimeException("Группа с таким именем уже существует");
            }
            group.setName(dto.getName());
        }

        if (dto.getCourseNumber() != null) {
            group.setCourseNumber(dto.getCourseNumber());
        }

        if (dto.getSpecialty() != null && !dto.getSpecialty().isBlank()) {
            group.setSpecialty(dto.getSpecialty());
        }

        if (dto.getStarostaId() != null) {
            StudentProfile starosta = studentProfileRepository.findById(dto.getStarostaId())
                    .orElseThrow(() -> new RuntimeException("Староста не найден"));

            if (starosta.getGroup() == null || starosta.getGroup().getId() != groupId) {
                throw new RuntimeException("Староста должен быть студентом этой группы");
            }

            group.setStarosta(starosta);
        }
        
        groupEntityRepository.save(group);
        return ResponseEntity.ok(new Response("Группа успешно обновлена"));
    }
        catch (Exception e) {   
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ошибка обновления группы");
        }
    }
    public void deleteGroup(int groupId) {
    GroupEntity group = groupEntityRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Группа не найдена"));

    StudentProfile starosta = group.getStarosta();

    if (starosta != null && starosta.getUser() != null) {
        User user = starosta.getUser();
        user.getRoles().remove(Role.GROUP_LEADER);
        userRepository.save(user);
    }

    group.setStarosta(null);
    groupEntityRepository.save(group);

    groupEntityRepository.delete(group);
    }
    public ResponseEntity<?> updateDiscipline(int id, AddDisciplineRequest dto){
        try{
        Discipline discipline = new Discipline();
        discipline.setId(id);
        discipline.setName(dto.getName());
        discipline.setDescription(dto.getDescription());
        disciplineRepository.save(discipline);
        return ResponseEntity.ok(new Response("Дисциплина обновлена"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ошибка обновлении"));
        }
    }
}

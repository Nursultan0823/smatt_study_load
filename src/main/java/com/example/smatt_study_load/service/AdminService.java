package com.example.smatt_study_load.service;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.smatt_study_load.DTO.AddDisciplineRequest;
import com.example.smatt_study_load.DTO.CurrentUserDto;
import com.example.smatt_study_load.DTO.GroupDTO;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.UserDto;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.UserDetailsImpl;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final GroupEntityRepository groupEntityRepository;
    private final DisciplineRepository disciplineRepository;
    public AdminService(GroupEntityRepository groupEntityRepository,
                        DisciplineRepository disciplineRepository,
                        UserRepository userRepository){
        this.groupEntityRepository=groupEntityRepository;
        this.disciplineRepository=disciplineRepository;
        this.userRepository=userRepository;
    }
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
}

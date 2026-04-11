package com.example.smatt_study_load.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.smatt_study_load.DTO.AddDisciplineRequest;
import com.example.smatt_study_load.DTO.ChangeStudentGroupRequest;
import com.example.smatt_study_load.DTO.GetDisciplineDTO;
import com.example.smatt_study_load.DTO.GetgroupDTO;
import com.example.smatt_study_load.DTO.GroupDTO;
import com.example.smatt_study_load.DTO.GroupStudentsResponseDto;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.ScheduleDto;
import com.example.smatt_study_load.DTO.UpdateGroupDto;
import com.example.smatt_study_load.DTO.UserDto;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.service.AdminService;

import lombok.AllArgsConstructor;

import java.util.List;



@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;
    private final GroupEntityRepository groupEntityRepository;
    private final DisciplineRepository disciplineRepository;

    @GetMapping("/pending")
    public List<UserDto> getPendingUsers() {
        return adminService.getPendingUser();
    }
@PutMapping("/approve/{id}")
public ResponseEntity<?> approveUser(@PathVariable int id) {
    User user = userRepository.findById(id).orElse(null);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response("Пользователь не найден"));
    }

    try {
        user.setStatus(UserStatus.APPROVED);
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new Response("Пользователь подтвержден"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("Ошибка при подтверждении пользователя"));
    }
}
    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable int id) {
         User user = userRepository.findById(id).orElse(null);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response("Пользователь не найден"));
    }

    try {
        user.setStatus(UserStatus.REJECTED);
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response("Пользователь не найден"));
    }
    
    }
      @PostMapping("/addgroup")
      public ResponseEntity<?> AddGroup(@RequestBody GroupDTO groupDTO){
        return adminService.AddGroupEntity(groupDTO);
      }
      @PostMapping("/adddiscipline")
      public ResponseEntity<?> postMethodName(@RequestBody AddDisciplineRequest request) {
          return adminService.AddDiscipline(request);
      }
       @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
       return adminService.getCurrentUser(authentication);
    }
     @GetMapping("/group/{groupId}")
    public GroupStudentsResponseDto getStudentsByGroupId(@PathVariable int groupId) {
        return adminService.getStudentsByGroupId(groupId);
    }
        @PutMapping("/group/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable int groupId,
            @RequestBody UpdateGroupDto dto
    ) {
        return adminService.updateGroup(groupId, dto);
    }
    @DeleteMapping("/group/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable int groupId) {
    try {
        adminService.deleteGroup(groupId);
    return ResponseEntity.ok(new Response("Группа удалена вместе со студентами и расписанием"));
    } catch (Exception e) {
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Не удолось удалить группу"));
    }
   
}
@PutMapping("/student/change-group")
public String changeStudentGroup(@RequestBody ChangeStudentGroupRequest request) {
    adminService.changeStudentGroup(request.getStudentId(), request.getGroupId());
    return "Группа студента успешно изменена";
}
    @GetMapping("/group")
    public List<GetgroupDTO> getGroup(){
        return groupEntityRepository.findAllGetgroup();
    }
     @GetMapping("/discipline")
    public List<GetDisciplineDTO> getDiscipline(){
        return disciplineRepository.findAllDisciplineDTO();
    }
           @PutMapping("/discipline/{disciplineId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable int disciplineId,
            @RequestBody AddDisciplineRequest dto
    ) {
        return adminService.updateDiscipline(disciplineId, dto);
    }
    @DeleteMapping("/discipline/{disciplineId}")
    public ResponseEntity<?> deleteDiscipline(@PathVariable int disciplineId){
        try{
        Discipline discipline = disciplineRepository.findById(disciplineId).orElse(null);
        disciplineRepository.delete(discipline);
        return ResponseEntity.ok(new Response("Дисциплина удалена"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("ошибка удаления "));
        }
    }
    @GetMapping("/group/{groupId}/schedules")
public List<ScheduleDto> getAllSchedulesByGroup(@PathVariable int groupId) {
    return adminService.getAllSchedulesByGroup(groupId);
}
}
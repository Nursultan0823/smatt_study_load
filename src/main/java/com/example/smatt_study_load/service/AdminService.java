package com.example.smatt_study_load.service;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smatt_study_load.DTO.AddDisciplineRequest;
import com.example.smatt_study_load.DTO.AdminManagedUserDto;
import com.example.smatt_study_load.DTO.CurrentUserDto;
import com.example.smatt_study_load.DTO.GroupDTO;
import com.example.smatt_study_load.DTO.GroupStudentsResponseDto;
import com.example.smatt_study_load.DTO.Response;
import com.example.smatt_study_load.DTO.ScheduleDto;
import com.example.smatt_study_load.DTO.UpdateGroupDto;
import com.example.smatt_study_load.DTO.UpdateManagedUserRequest;
import com.example.smatt_study_load.DTO.UserDto;
import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.models.Roles;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.RoleRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UserRepository;
import com.example.smatt_study_load.utils.StudentProfileMapper;
import com.example.smatt_study_load.utils.UserDetailsImpl;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final GroupEntityRepository groupEntityRepository;
    private final DisciplineRepository disciplineRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ScheduleRepository scheduleRepository;
    private final RoleRepository roleRepository;
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

    @Transactional(readOnly = true)
    public List<AdminManagedUserDto> getManagedUsers(String search, String role, String status) {
        String normalizedSearch = normalize(search);
        String normalizedRole = normalize(role);
        UserStatus normalizedStatus = parseStatus(status);

        return userRepository.findAllByStatusNotOrderByFullNameAsc(UserStatus.DELETED).stream()
                .filter(user -> normalizedStatus == null || user.getStatus() == normalizedStatus)
                .filter(user -> normalizedRole == null || hasRoleName(user, normalizedRole))
                .filter(user -> normalizedSearch == null || matchesSearch(user, normalizedSearch))
                .map(this::toManagedUserDto)
                .toList();
    }

    @Transactional
    public AdminManagedUserDto updateManagedUser(int userId, UpdateManagedUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь удален");
        }

        if (request.getGroupId() != null || request.getGroupLeader() != null) {
            StudentProfile student = studentProfileRepository.findByUser(user)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Группу и статус старосты можно менять только студенту"));

            if (request.getGroupId() != null) {
                changeStudentGroupInternal(student, request.getGroupId());
            }

            if (request.getGroupLeader() != null) {
                if (request.getGroupLeader()) {
                    assignGroupLeader(student);
                } else {
                    clearGroupLeader(student);
                }
            }
        }

        if (request.getTeacherPosition() != null) {
            TeacherProfile teacher = teacherProfileRepository.findByUser(user)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Должность можно менять только преподавателю"));
            teacher.setPosition(normalizeOptional(request.getTeacherPosition()));
            teacherProfileRepository.save(teacher);
        }

        return toManagedUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")));
    }

    @Transactional
    public void deleteManagedUser(int userId, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        if (currentUser.getId() == userId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя удалить текущего администратора");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        studentProfileRepository.findByUser(user).ifPresent(this::clearGroupLeader);

        user.setEnabled(false);
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    @Transactional
    public ResponseEntity<?> approveUser(int id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response("Пользователь не найден"));
        }

        if (user.getStatus() != UserStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response("Подтвердить можно только заявку в ожидании"));
        }

        user.setStatus(UserStatus.APPROVED);
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new Response("Пользователь подтвержден"));
    }

    @Transactional
    public ResponseEntity<?> rejectUser(int id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response("Пользователь не найден"));
        }

        if (user.getStatus() != UserStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response("Отклонить можно только заявку в ожидании"));
        }

        studentProfileRepository.findByUser(user).ifPresent(this::removeStudentProfileFromGroup);

        user.setStatus(UserStatus.REJECTED);
        user.setEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok(new Response("Заявка отклонена"));
    }

    public GroupStudentsResponseDto getStudentsByGroupId(int groupId) {
        GroupEntity group = groupEntityRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        List<StudentProfile> students = studentProfileRepository.findAllByGroupIdWithUserAndGroup(
                groupId,
                UserStatus.APPROVED
        );

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
            Roles groupLeaderRole = roleRepository.findByName(Role.GROUP_LEADER)
                    .orElseThrow(() -> new RuntimeException("Роль GROUP_LEADER не найдена"));

            StudentProfile oldStarosta = group.getStarosta();
            if (oldStarosta != null && oldStarosta.getUser() != null) {
                oldStarosta.getUser().getRoles().remove(groupLeaderRole);
                userRepository.save(oldStarosta.getUser());
            }

            if (dto.getStarostaId() == 0) {
                group.setStarosta(null);
            } else {
                StudentProfile starosta = studentProfileRepository.findById(dto.getStarostaId())
                        .orElseThrow(() -> new RuntimeException("Староста не найден"));

                if (starosta.getGroup() == null || starosta.getGroup().getId() != groupId) {
                    throw new RuntimeException("Староста должен быть студентом этой группы");
                }

                if (!isApprovedStudent(starosta)) {
                    throw new RuntimeException("Старостой может быть только подтвержденный студент");
                }

                starosta.getUser().getRoles().add(groupLeaderRole);
                userRepository.save(starosta.getUser());
                group.setStarosta(starosta);
            }
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
        Roles groupLeaderRole = roleRepository.findByName(Role.GROUP_LEADER)
                .orElse(null);
        if (groupLeaderRole != null) {
            User user = starosta.getUser();
            user.getRoles().remove(groupLeaderRole);
            userRepository.save(user);
        }
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
    @Transactional
    public void changeStudentGroup(int studentId, int groupId) {
        StudentProfile student = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        changeStudentGroupInternal(student, groupId);
    }

private void changeStudentGroupInternal(StudentProfile student, int groupId) {
    if (!isApprovedStudent(student)) {
        throw new RuntimeException("Группу можно изменить только подтвержденному студенту");
    }

    GroupEntity oldGroup = student.getGroup();
    if (oldGroup != null && oldGroup.getStarosta() != null
            && oldGroup.getStarosta().getId() == student.getId()) {
        oldGroup.setStarosta(null);
        groupEntityRepository.save(oldGroup);
        removeGroupLeaderRole(student);
    }

    GroupEntity newGroup = groupEntityRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Группа не найдена"));

    student.setGroup(newGroup);
    studentProfileRepository.save(student);
}

private void assignGroupLeader(StudentProfile student) {
    if (!isApprovedStudent(student)) {
        throw new RuntimeException("Старостой может быть только подтвержденный студент");
    }

    GroupEntity group = student.getGroup();
    if (group == null) {
        throw new RuntimeException("Студент не состоит в группе");
    }

    Roles groupLeaderRole = roleRepository.findByName(Role.GROUP_LEADER)
            .orElseThrow(() -> new RuntimeException("Роль GROUP_LEADER не найдена"));

    StudentProfile oldStarosta = group.getStarosta();
    if (oldStarosta != null && oldStarosta.getId() != student.getId()
            && oldStarosta.getUser() != null) {
        oldStarosta.getUser().getRoles().remove(groupLeaderRole);
        userRepository.save(oldStarosta.getUser());
    }

    student.getUser().getRoles().add(groupLeaderRole);
    userRepository.save(student.getUser());
    group.setStarosta(student);
    groupEntityRepository.save(group);
}

private void clearGroupLeader(StudentProfile student) {
    GroupEntity group = student.getGroup();
    if (group != null && group.getStarosta() != null
            && group.getStarosta().getId() == student.getId()) {
        group.setStarosta(null);
        groupEntityRepository.save(group);
    }

    removeGroupLeaderRole(student);
}

private void removeGroupLeaderRole(StudentProfile student) {
    if (student.getUser() == null) {
        return;
    }

    roleRepository.findByName(Role.GROUP_LEADER).ifPresent(role -> {
        student.getUser().getRoles().remove(role);
        userRepository.save(student.getUser());
    });
}

public List<ScheduleDto> getAllSchedulesByGroup(int groupId) {
    List<Schedule> schedules = scheduleRepository.findByGroupIdOrderByDayOfWeekAscStartTimeAsc(groupId);

    return schedules.stream()
            .map(schedule -> new ScheduleDto(
                    schedule.getId(),
                    schedule.getDayOfWeek().name(),
                    schedule.getStartTime().toString(),
                    schedule.getEndTime().toString(),
                    schedule.getRoom(),
                    schedule.getDiscipline().getName(),
                    getTeacherName(schedule.getTeacher()),
                    schedule.getTeacher().getPosition(),
                    schedule.getUrl()
            ))
            .toList();
}
private String getTeacherName(TeacherProfile teacher) {
    if (teacher == null || teacher.getUser() == null) {
        return "Неизвестно";
    }

    return teacher.getUser().getFullName();
}

private AdminManagedUserDto toManagedUserDto(User user) {
    AdminManagedUserDto dto = new AdminManagedUserDto();
    dto.setId(user.getId());
    dto.setFullName(user.getFullName());
    dto.setEmail(user.getEmail());
    dto.setStatus(user.getStatus().name());
    dto.setEnabled(user.isEnabled());
    dto.setConfirmed(user.isEnabled() && user.getStatus() == UserStatus.APPROVED);
    dto.setRoles(user.getRoles().stream()
            .map(role -> role.getName().name())
            .sorted()
            .toList());
    dto.setPrimaryRole(resolvePrimaryRole(user));

    StudentProfile student = user.getStudentProfile();
    if (student != null) {
        dto.setStudentProfileId(student.getId());
        GroupEntity group = student.getGroup();
        if (group != null) {
            dto.setGroupId(group.getId());
            dto.setGroupName(group.getName());
            dto.setGroupLeader(group.getStarosta() != null
                    && group.getStarosta().getId() == student.getId());
        }
    }

    TeacherProfile teacher = user.getTeacherProfile();
    if (teacher != null) {
        dto.setTeacherProfileId(teacher.getId());
        dto.setTeacherPosition(teacher.getPosition());
        dto.setTeacherPhoneNumber(teacher.getPhoneNumber());
        dto.setTeacherWhatsApp(teacher.getWhatsApp());
    }

    return dto;
}

private boolean matchesSearch(User user, String search) {
    String value = search.toLowerCase();
    return contains(user.getFullName(), value)
            || contains(user.getEmail(), value)
            || contains(user.getStudentProfile() != null && user.getStudentProfile().getGroup() != null
                    ? user.getStudentProfile().getGroup().getName()
                    : null, value)
            || contains(user.getTeacherProfile() != null ? user.getTeacherProfile().getPosition() : null, value);
}

private boolean contains(String source, String value) {
    return source != null && source.toLowerCase().contains(value);
}

private boolean hasRoleName(User user, String roleName) {
    return user.getRoles().stream()
            .anyMatch(role -> role.getName().name().equalsIgnoreCase(roleName));
}

private String resolvePrimaryRole(User user) {
    if (hasRoleName(user, Role.ADMIN.name())) {
        return Role.ADMIN.name();
    }
    if (hasRoleName(user, Role.TEACHER.name())) {
        return Role.TEACHER.name();
    }
    if (hasRoleName(user, Role.GROUP_LEADER.name())) {
        return Role.GROUP_LEADER.name();
    }
    if (hasRoleName(user, Role.STUDENT.name())) {
        return Role.STUDENT.name();
    }
    return null;
}

private UserStatus parseStatus(String value) {
    String normalized = normalize(value);
    if (normalized == null) {
        return null;
    }

    try {
        return UserStatus.valueOf(normalized.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный статус пользователя");
    }
}

private User getAuthenticatedUser(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
    }

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
}

private String normalize(String value) {
    if (value == null) {
        return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
}

private String normalizeOptional(String value) {
    String normalized = normalize(value);
    return normalized == null ? null : normalized;
}

private boolean isApprovedStudent(StudentProfile studentProfile) {
    return studentProfile != null
            && studentProfile.getUser() != null
            && studentProfile.getUser().isEnabled()
            && studentProfile.getUser().getStatus() == UserStatus.APPROVED;
}

private void removeStudentProfileFromGroup(StudentProfile studentProfile) {
    GroupEntity group = studentProfile.getGroup();
    Roles groupLeaderRole = roleRepository.findByName(Role.GROUP_LEADER).orElse(null);
    User studentUser = studentProfile.getUser();

    if (group != null && group.getStarosta() != null
            && group.getStarosta().getId() == studentProfile.getId()) {
        group.setStarosta(null);
        groupEntityRepository.save(group);
    }

    if (studentUser != null && groupLeaderRole != null) {
        studentUser.getRoles().remove(groupLeaderRole);
    }

    if (studentUser != null) {
        studentUser.setStudentProfile(null);
        userRepository.save(studentUser);
    }

    studentProfileRepository.delete(studentProfile);
}
}

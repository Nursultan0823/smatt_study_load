package com.example.smatt_study_load.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.smatt_study_load.enums.Role;
import com.example.smatt_study_load.enums.UserStatus;
import com.example.smatt_study_load.models.Discipline;
import com.example.smatt_study_load.models.GroupEntity;
import com.example.smatt_study_load.models.Roles;
import com.example.smatt_study_load.models.Schedule;
import com.example.smatt_study_load.models.StudentProfile;
import com.example.smatt_study_load.models.TeacherProfile;
import com.example.smatt_study_load.models.UmmMaterial;
import com.example.smatt_study_load.models.UmmMaterialKind;
import com.example.smatt_study_load.models.User;
import com.example.smatt_study_load.repository.DisciplineRepository;
import com.example.smatt_study_load.repository.GroupEntityRepository;
import com.example.smatt_study_load.repository.RoleRepository;
import com.example.smatt_study_load.repository.ScheduleRepository;
import com.example.smatt_study_load.repository.StudentProfileRepository;
import com.example.smatt_study_load.repository.TeacherProfileRepository;
import com.example.smatt_study_load.repository.UmmMaterialRepository;
import com.example.smatt_study_load.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      RoleRepository roleRepository,
                                      TeacherProfileRepository teacherProfileRepository,
                                      StudentProfileRepository studentProfileRepository,
                                      GroupEntityRepository groupRepository,
                                      DisciplineRepository disciplineRepository,
                                      ScheduleRepository scheduleRepository,
                                      UmmMaterialRepository ummMaterialRepository) {
        return args -> {

            initRoles(roleRepository);
            initAdmin(userRepository, passwordEncoder, roleRepository);
            initGroups(groupRepository);
            initTeachers(userRepository, passwordEncoder, roleRepository, teacherProfileRepository);
            initStudents(userRepository, passwordEncoder, roleRepository, studentProfileRepository, groupRepository);
            initDisciplines(disciplineRepository);
            initSchedules(groupRepository, disciplineRepository, teacherProfileRepository, scheduleRepository);
            initUmmMaterials(disciplineRepository, teacherProfileRepository, ummMaterialRepository);

            System.out.println("Инициализация данных завершена");
        };
    }

    private void initRoles(RoleRepository roleRepository) {
        for (Role roleName : Role.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Roles role = new Roles();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void initAdmin(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository) {
        String adminEmail = "admin@example.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            Roles adminRole = roleRepository.findByName(Role.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена"));

            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(new HashSet<>(Set.of(adminRole)));
            admin.setStatus(UserStatus.APPROVED);
            admin.setEnabled(true);

            userRepository.save(admin);
            System.out.println("Администратор создан");
        } else {
            System.out.println("Администратор уже существует");
        }
    }

    private void initGroups(GroupEntityRepository groupRepository) {
        if (groupRepository.findByName("ИВТ-21-1").isEmpty()) {
            GroupEntity group1 = new GroupEntity();
            group1.setName("ИВТ-21-1");
            group1.setCourseNumber(3);
            group1.setSpecialty("Информатика и вычислительная техника");
            groupRepository.save(group1);
            System.out.println("Группа ИВТ-21-1 создана");
        }

        if (groupRepository.findByName("ИВТ-21-2").isEmpty()) {
            GroupEntity group2 = new GroupEntity();
            group2.setName("ИВТ-21-2");
            group2.setCourseNumber(3);
            group2.setSpecialty("Информатика и вычислительная техника");
            groupRepository.save(group2);
            System.out.println("Группа ИВТ-21-2 создана");
        }
    }

    private void initTeachers(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              RoleRepository roleRepository,
                              TeacherProfileRepository teacherProfileRepository) {
        Roles teacherRole = roleRepository.findByName(Role.TEACHER)
                .orElseThrow(() -> new RuntimeException("Роль TEACHER не найдена"));

        createTeacherIfNotExists(
                "teacher1@example.com",
                "Иванов Иван Иванович",
                "123456",
                "доцент",
                userRepository,
                passwordEncoder,
                teacherRole,
                teacherProfileRepository
        );

        createTeacherIfNotExists(
                "teacher2@example.com",
                "Петров Петр Петрович",
                "123456",
                "преподаватель",
                userRepository,
                passwordEncoder,
                teacherRole,
                teacherProfileRepository
        );
    }

    private void createTeacherIfNotExists(String email,
                                          String fullName,
                                          String password,
                                          String position,
                                          UserRepository userRepository,
                                          PasswordEncoder passwordEncoder,
                                          Roles teacherRole,
                                          TeacherProfileRepository teacherProfileRepository) {
        if (userRepository.existsByEmail(email)) {
            System.out.println("Преподаватель с email " + email + " уже существует");
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(Set.of(teacherRole)));
        user.setStatus(UserStatus.APPROVED);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        TeacherProfile teacherProfile = new TeacherProfile();
        teacherProfile.setUser(savedUser);
        teacherProfile.setPosition(position);

        teacherProfileRepository.save(teacherProfile);
        System.out.println("Преподаватель " + fullName + " создан");
    }

    private void initStudents(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              RoleRepository roleRepository,
                              StudentProfileRepository studentProfileRepository,
                              GroupEntityRepository groupRepository) {
        Roles studentRole = roleRepository.findByName(Role.STUDENT)
                .orElseThrow(() -> new RuntimeException("Роль STUDENT не найдена"));

        GroupEntity group1 = groupRepository.findByName("ИВТ-21-1")
                .orElseThrow(() -> new RuntimeException("Группа ИВТ-21-1 не найдена"));

        GroupEntity group2 = groupRepository.findByName("ИВТ-21-2")
                .orElseThrow(() -> new RuntimeException("Группа ИВТ-21-2 не найдена"));

        createStudentIfNotExists(
                "student1@example.com",
                "Садыков Нурсултан",
                "123456",
                group1,
                userRepository,
                passwordEncoder,
                studentRole,
                studentProfileRepository
        );

        createStudentIfNotExists(
                "student2@example.com",
                "Асанов Бакыт",
                "123456",
                group2,
                userRepository,
                passwordEncoder,
                studentRole,
                studentProfileRepository
        );
    }

    private void createStudentIfNotExists(String email,
                                          String fullName,
                                          String password,
                                          GroupEntity group,
                                          UserRepository userRepository,
                                          PasswordEncoder passwordEncoder,
                                          Roles studentRole,
                                          StudentProfileRepository studentProfileRepository) {
        if (userRepository.existsByEmail(email)) {
            System.out.println("Студент с email " + email + " уже существует");
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(Set.of(studentRole)));
        user.setStatus(UserStatus.APPROVED);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setUser(savedUser);
        studentProfile.setGroup(group);

        studentProfileRepository.save(studentProfile);
        System.out.println("Студент " + fullName + " создан");
    }

    private void initDisciplines(DisciplineRepository disciplineRepository) {
        if (disciplineRepository.findByName("Программирование").isEmpty()) {
            Discipline d1 = new Discipline();
            d1.setName("Программирование");
            d1.setDescription("Изучение основ объектно-ориентированного программирования");
            disciplineRepository.save(d1);
            System.out.println("Дисциплина Программирование создана");
        }

        if (disciplineRepository.findByName("Базы данных").isEmpty()) {
            Discipline d2 = new Discipline();
            d2.setName("Базы данных");
            d2.setDescription("Изучение SQL и проектирования баз данных");
            disciplineRepository.save(d2);
            System.out.println("Дисциплина Базы данных создана");
        }
    }

    private void initSchedules(GroupEntityRepository groupRepository,
                               DisciplineRepository disciplineRepository,
                               TeacherProfileRepository teacherProfileRepository,
                               ScheduleRepository scheduleRepository) {

        GroupEntity group1 = groupRepository.findByName("ИВТ-21-1")
                .orElseThrow(() -> new RuntimeException("Группа ИВТ-21-1 не найдена"));

        GroupEntity group2 = groupRepository.findByName("ИВТ-21-2")
                .orElseThrow(() -> new RuntimeException("Группа ИВТ-21-2 не найдена"));

        Discipline programming = disciplineRepository.findByName("Программирование")
                .orElseThrow(() -> new RuntimeException("Дисциплина Программирование не найдена"));

        Discipline databases = disciplineRepository.findByName("Базы данных")
                .orElseThrow(() -> new RuntimeException("Дисциплина Базы данных не найдена"));

        TeacherProfile teacher1 = teacherProfileRepository.findByUserEmail("teacher1@example.com")
                .orElseThrow(() -> new RuntimeException("Преподаватель teacher1@example.com не найден"));

        TeacherProfile teacher2 = teacherProfileRepository.findByUserEmail("teacher2@example.com")
                .orElseThrow(() -> new RuntimeException("Преподаватель teacher2@example.com не найден"));

        if (!scheduleRepository.existsByGroupAndDisciplineAndTeacherAndDayOfWeekAndStartTime(
                group1, programming, teacher1, DayOfWeek.MONDAY, LocalTime.of(9, 0))) {
            Schedule s1 = new Schedule();
            s1.setGroup(group1);
            s1.setDiscipline(programming);
            s1.setTeacher(teacher1);
            s1.setDayOfWeek(DayOfWeek.MONDAY);
            s1.setStartTime(LocalTime.of(9, 0));
            s1.setEndTime(LocalTime.of(10, 30));
            s1.setRoom("101");
            s1.setUrl(null);
            scheduleRepository.save(s1);
            System.out.println("Расписание 1 создано");
        }

        if (!scheduleRepository.existsByGroupAndDisciplineAndTeacherAndDayOfWeekAndStartTime(
                group2, databases, teacher2, DayOfWeek.TUESDAY, LocalTime.of(11, 0))) {
            Schedule s2 = new Schedule();
            s2.setGroup(group2);
            s2.setDiscipline(databases);
            s2.setTeacher(teacher2);
            s2.setDayOfWeek(DayOfWeek.TUESDAY);
            s2.setStartTime(LocalTime.of(11, 0));
            s2.setEndTime(LocalTime.of(12, 30));
            s2.setRoom("202");
            s2.setUrl(null);
            scheduleRepository.save(s2);
            System.out.println("Расписание 2 создано");
        }
    }

    private void initUmmMaterials(DisciplineRepository disciplineRepository,
                                  TeacherProfileRepository teacherProfileRepository,
                                  UmmMaterialRepository ummMaterialRepository) {

        Discipline programming = disciplineRepository.findByName("Программирование")
                .orElseThrow(() -> new RuntimeException("Дисциплина Программирование не найдена"));

        Discipline databases = disciplineRepository.findByName("Базы данных")
                .orElseThrow(() -> new RuntimeException("Дисциплина Базы данных не найдена"));

        TeacherProfile teacher1 = teacherProfileRepository.findByUserEmail("teacher1@example.com")
                .orElseThrow(() -> new RuntimeException("Преподаватель teacher1@example.com не найден"));

        TeacherProfile teacher2 = teacherProfileRepository.findByUserEmail("teacher2@example.com")
                .orElseThrow(() -> new RuntimeException("Преподаватель teacher2@example.com не найден"));

        LocalDateTime now = LocalDateTime.now();

        // --- Программирование (teacher1) ---
        createUmmIfNotExists(
                "Лекция 1. Введение в объектно-ориентированное программирование",
                "Базовые понятия ООП: классы, объекты, инкапсуляция, наследование, полиморфизм. "
                        + "Рассматриваются отличия процедурного и объектно-ориентированного подходов, "
                        + "а также практические примеры на Java.",
                programming,
                teacher1,
                List.of(
                        "https://docs.oracle.com/javase/tutorial/java/concepts/",
                        "https://ru.wikipedia.org/wiki/Объектно-ориентированное_программирование"
                ),
                now.minusDays(30),
                UmmMaterialKind.LECTURE,
                "Объектно-ориентированное программирование",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Лекция 2. Принципы SOLID",
                "Пять принципов проектирования, позволяющих создавать поддерживаемый и расширяемый код: "
                        + "SRP, OCP, LSP, ISP, DIP. Каждый принцип разобран на примерах.",
                programming,
                teacher1,
                List.of(
                        "https://habr.com/ru/articles/688530/",
                        "https://en.wikipedia.org/wiki/SOLID"
                ),
                now.minusDays(25),
                UmmMaterialKind.LECTURE,
                "Принципы проектирования",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Методичка по коллекциям Java",
                "Обзор основных интерфейсов и реализаций java.util: List, Set, Map, Queue. "
                        + "Таблицы сравнения реализаций по сложности операций, рекомендации по выбору.",
                programming,
                teacher1,
                List.of(
                        "https://docs.oracle.com/javase/tutorial/collections/",
                        "https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html"
                ),
                now.minusDays(20),
                UmmMaterialKind.UMK,
                "Коллекции и обобщённое программирование",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Практика: Шаблоны проектирования GoF",
                "Классические паттерны: порождающие, структурные, поведенческие. "
                        + "Для каждого шаблона приведены мотивация, структура и пример применения.",
                programming,
                teacher1,
                List.of(
                        "https://refactoring.guru/ru/design-patterns",
                        "https://ru.wikipedia.org/wiki/Design_Patterns"
                ),
                now.minusDays(15),
                UmmMaterialKind.LAB,
                "Паттерны проектирования",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Лекция: Исключения и их обработка",
                "Иерархия Throwable, checked и unchecked исключения, конструкция try-with-resources, "
                        + "рекомендации по проектированию пользовательских исключений.",
                programming,
                teacher1,
                List.of(
                        "https://docs.oracle.com/javase/tutorial/essential/exceptions/"
                ),
                now.minusDays(10),
                UmmMaterialKind.LECTURE,
                "Исключения и ошибки",
                ummMaterialRepository
        );

        // --- Базы данных (teacher2) ---
        createUmmIfNotExists(
                "Лекция 1. Введение в реляционные СУБД",
                "Понятие реляционной модели данных, основные объекты БД, архитектура клиент-сервер. "
                        + "Обзор популярных СУБД: PostgreSQL, MySQL, Oracle, MS SQL Server.",
                databases,
                teacher2,
                List.of(
                        "https://www.postgresql.org/docs/current/tutorial.html",
                        "https://ru.wikipedia.org/wiki/Реляционная_СУБД"
                ),
                now.minusDays(28),
                UmmMaterialKind.LECTURE,
                "Основы реляционных СУБД",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Методичка по SQL: выборка данных (SELECT)",
                "Синтаксис SELECT, условия WHERE, соединения JOIN, группировки GROUP BY/HAVING, "
                        + "подзапросы. Разобраны типичные задачи с решениями.",
                databases,
                teacher2,
                List.of(
                        "https://www.postgresql.org/docs/current/sql-select.html",
                        "https://www.w3schools.com/sql/"
                ),
                now.minusDays(22),
                UmmMaterialKind.UMK,
                "Язык SQL",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Лекция: Нормализация баз данных",
                "Нормальные формы: 1НФ, 2НФ, 3НФ, BCNF. Аномалии избыточности и способы их устранения. "
                        + "Практические рекомендации при проектировании схемы БД.",
                databases,
                teacher2,
                List.of(
                        "https://ru.wikipedia.org/wiki/Нормальная_форма",
                        "https://habr.com/ru/articles/254773/"
                ),
                now.minusDays(18),
                UmmMaterialKind.LECTURE,
                "Проектирование схемы БД",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Практика: Индексы и оптимизация запросов",
                "Типы индексов (B-tree, Hash, GIN), правила использования EXPLAIN / EXPLAIN ANALYZE, "
                        + "анти-паттерны запросов и способы их оптимизации.",
                databases,
                teacher2,
                List.of(
                        "https://www.postgresql.org/docs/current/indexes.html",
                        "https://use-the-index-luke.com/"
                ),
                now.minusDays(12),
                UmmMaterialKind.LAB,
                "Оптимизация запросов",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Лекция: Транзакции и свойства ACID",
                "Понятие транзакции, свойства ACID, уровни изоляции (READ COMMITTED, REPEATABLE READ, "
                        + "SERIALIZABLE), типичные аномалии: грязное чтение, неповторяющееся чтение, фантомы.",
                databases,
                teacher2,
                List.of(
                        "https://www.postgresql.org/docs/current/transaction-iso.html",
                        "https://ru.wikipedia.org/wiki/ACID"
                ),
                now.minusDays(5),
                UmmMaterialKind.LECTURE,
                "Транзакции и изоляция",
                ummMaterialRepository
        );

        createUmmIfNotExists(
                "Глоссарий терминов реляционных СУБД",
                "Краткие определения: кортеж, отношение, ключ, внешний ключ, ссылочная целостность, "
                        + "представление, триггер, хранимая процедура.",
                databases,
                teacher2,
                List.of("https://www.postgresql.org/docs/current/glossary.html"),
                now.minusDays(2),
                UmmMaterialKind.EXTRA,
                "Справочная информация",
                ummMaterialRepository
        );
    }

    private void createUmmIfNotExists(String title,
                                      String description,
                                      Discipline discipline,
                                      TeacherProfile author,
                                      List<String> urls,
                                      LocalDateTime createdAt,
                                      UmmMaterialKind materialKind,
                                      String section,
                                      UmmMaterialRepository ummMaterialRepository) {
        boolean alreadyExists = ummMaterialRepository.findAll().stream()
                .anyMatch(m -> title.equals(m.getTitle())
                        && m.getDiscipline() != null
                        && m.getDiscipline().getId() == discipline.getId());

        if (alreadyExists) {
            return;
        }

        UmmMaterial material = new UmmMaterial();
        material.setTitle(title);
        material.setDescription(description);
        material.setDiscipline(discipline);
        material.setAuthor(author);
        material.setCreatedAt(createdAt);
        material.setUpdatedAt(createdAt);
        material.setMaterialKind(materialKind != null ? materialKind : UmmMaterialKind.GENERAL);
        if (section != null && !section.isBlank()) {
            material.setSection(section.trim());
        }
        material.setUrlList(new ArrayList<>(urls));
        ummMaterialRepository.save(material);
        System.out.println("УММ создан: " + title);
    }
}
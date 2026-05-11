package com.example.smatt_study_load.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Getter
@Setter
@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String position;   // преподаватель, доцент и т.д.

    @Column(length = 40)
    private String phoneNumber;

    @Column(length = 40)
    private String whatsApp;

     @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();
}

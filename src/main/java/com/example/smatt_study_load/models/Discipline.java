package com.example.smatt_study_load.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Data
@Entity
@Table(name = "disciplines")
public class Discipline {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();


    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UmmFile> ummFiles = new ArrayList<>();
}

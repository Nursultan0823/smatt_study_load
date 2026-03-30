package com.example.smatt_study_load.models;

import com.example.smatt_study_load.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Roles {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

     @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Role name;

}

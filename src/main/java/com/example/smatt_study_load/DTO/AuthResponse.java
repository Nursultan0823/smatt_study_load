package com.example.smatt_study_load.DTO;

import java.util.Set;

import com.example.smatt_study_load.models.Roles;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Integer id;
       private Set<Roles> roles;
}
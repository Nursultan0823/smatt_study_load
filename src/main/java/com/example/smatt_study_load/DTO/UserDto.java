package com.example.smatt_study_load.DTO;

import lombok.Data;

@Data
public class UserDto {
     private Integer id;
     private String fullName;
     private String email;
    private String status;
      public UserDto(Integer id, String fullName, String email, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

}

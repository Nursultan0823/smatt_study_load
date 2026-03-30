package com.example.smatt_study_load.DTO;

public class GroupShortDto {

    private Integer id;
    private String name;

    public GroupShortDto() {
    }

    public GroupShortDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
package com.example.smatt_study_load.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "umm_files")
public class UmmFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String filePath;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "discipline_id", nullable = false)
    private Discipline discipline;
}

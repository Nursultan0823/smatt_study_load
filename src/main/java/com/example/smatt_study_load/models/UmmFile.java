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
    private String fileType;
    @Column(nullable = false)
    private String fileName;
    @Lob
    @Column(name = "file_data")
    private byte[] fileData;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "discipline_id", nullable = false)
    private Discipline discipline;
}

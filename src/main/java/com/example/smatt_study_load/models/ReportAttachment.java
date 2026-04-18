package com.example.smatt_study_load.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "report_attachments")
public class ReportAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fileName;

    private String contentType;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;
}

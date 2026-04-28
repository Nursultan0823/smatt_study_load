package com.example.smatt_study_load.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "announcement_read_status",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"announcement_id", "user_id"})
        }
)
public class AnnouncementReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean seen = false;

    private LocalDateTime seenAt;
}

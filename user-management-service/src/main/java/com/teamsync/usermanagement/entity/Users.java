package com.teamsync.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_picture_url")
    private String profilePicture;

    @Column(name = "profile_picture_data")
    private byte[] profilePictureData;

    private String designation;

    private LocalDate birthdate;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "predicted_burnout_risk")
    private Boolean predictedBurnoutRisk;
}
package com.teamsync.projectmanagementservice.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectMemberId implements Serializable {
    private Long project;  // matches the field name in ProjectMembers
    private Long user;     // matches the field name in ProjectMembers
}
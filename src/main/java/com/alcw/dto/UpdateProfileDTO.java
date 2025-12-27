package com.alcw.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String name;
    private String occupation;
    private String password;
}
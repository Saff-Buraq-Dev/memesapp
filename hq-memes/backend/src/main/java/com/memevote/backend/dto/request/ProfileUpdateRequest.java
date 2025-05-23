package com.memevote.backend.dto.request;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ProfileUpdateRequest {
    @Size(min = 3, max = 20)
    private String username;
}

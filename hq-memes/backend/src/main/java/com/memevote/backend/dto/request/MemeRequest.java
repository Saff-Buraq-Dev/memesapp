package com.memevote.backend.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class MemeRequest {
    @NotBlank
    @Size(max = 100)
    private String title;
    
    private Set<String> categories;
}

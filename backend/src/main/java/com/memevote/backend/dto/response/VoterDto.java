package com.memevote.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoterDto {
    private Long id;
    private String username;
    private String profilePicture;
}

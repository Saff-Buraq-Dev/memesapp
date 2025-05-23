package com.memevote.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String profilePicture;

    public JwtResponse(String token, Long id, String username, String email, String profilePicture) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
    }
}

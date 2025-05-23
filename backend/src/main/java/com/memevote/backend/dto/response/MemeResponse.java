package com.memevote.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemeResponse {
    private Long id;
    private String title;
    private String url;
    private LocalDateTime createdAt;
    private UserSummary user;
    private Set<CategoryDto> categories;
    private Long voteCount;
    private Boolean userVoted;
    private List<VoterDto> voters;
}

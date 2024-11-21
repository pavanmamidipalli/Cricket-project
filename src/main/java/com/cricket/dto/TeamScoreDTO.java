package com.cricket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamScoreDTO extends BaseResponseDTO{
    private String teamName;
    private int totalScore;
}

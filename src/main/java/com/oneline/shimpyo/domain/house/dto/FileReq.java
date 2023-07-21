package com.oneline.shimpyo.domain.house.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FileReq {
    private String originalFileName;
    private String savedURL;
}

package com.oneline.shimpyo.domain.house.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OptionReq {
    private boolean wifi;
    private boolean pc;
    private boolean parking;
    private boolean bbq;
}

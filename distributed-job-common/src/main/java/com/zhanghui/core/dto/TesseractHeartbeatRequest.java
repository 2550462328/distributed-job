package com.zhanghui.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TesseractHeartbeatRequest {
    @NotBlank
    private String socket;
    @NotNull
    Integer activeCount;
    @NotNull
    Integer maximumPoolSize;
    @NotNull
    private Set<String> clientJobGroups;
}

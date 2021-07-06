package org.thingsboard.server.dft.controllers.web.khachhang.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CameraSetting {
    @NotNull(message = "required")
    private String cameraSever;

    @NotNull(message = "required")
    private String apiKey;

    @NotNull(message = "required")
    private String signUpPath;

    @NotNull(message = "required")
    private String deletePath;
}

package org.thingsboard.server.dft.controllers.web.users.dtos;

import javax.validation.constraints.NotBlank;

public class ActivateAccountDto {

    @NotBlank
    private String email;

    @NotBlank
    private String activateCode;

    public ActivateAccountDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActivateCode() {
        return activateCode;
    }

    public void setActivateCode(String activateCode) {
        this.activateCode = activateCode;
    }
}

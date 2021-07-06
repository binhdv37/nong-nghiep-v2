package org.thingsboard.server.dft.controllers.web.role.dtos;

import java.util.List;

public class ErrorInfoDto {
    private int code;
    private String message;

    /*
        - code = 1 : validate dto loi
        - code = 2 : ten vai tro da ton tai
     */

    public ErrorInfoDto() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}



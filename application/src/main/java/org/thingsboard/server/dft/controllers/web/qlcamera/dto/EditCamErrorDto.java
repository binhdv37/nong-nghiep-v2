package org.thingsboard.server.dft.controllers.web.qlcamera.dto;

public class EditCamErrorDto {
    private int code;
    private String message; // frontend k dựa vào message này để hiện thông báo mà dựa vào code

    /*
        - code 1 : mã camera đã tồn tại
        - code 2 : tên camera đã tồn tại
     */

    public EditCamErrorDto() {
    }

    public EditCamErrorDto(int code, String message) {
        this.code = code;
        this.message = message;
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

package org.thingsboard.server.dft.controllers.web.users.dtos;

import javax.validation.constraints.NotNull;

// cap nhat thong tin ca nhan tai khoan nguoi dung (ten, so dien thoai)
public class MbUpdateUserDto {

    @NotNull
    private String fullName;

    @NotNull
    private String phoneNumber;

    public MbUpdateUserDto() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

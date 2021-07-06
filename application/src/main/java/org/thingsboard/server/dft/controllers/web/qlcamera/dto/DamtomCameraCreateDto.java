package org.thingsboard.server.dft.controllers.web.qlcamera.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class DamtomCameraCreateDto {

    private UUID id;

    @NotNull(message = "damtomid không được để trống")
    private UUID damtomId;

    @NotBlank(message = "Mã camera không được để trống")
    @Length(max = 50, message = "Mã camera không được quá 50 kí tự")
    private String code;

    @NotBlank(message = "Tên camera không được để trống")
    @Length(max = 255, message = "Tên camera không được quá 255 kí tự")
    private String name;

    @NotBlank(message = "Url không được để trống")
    @Length(max = 255, message = "Url không được quá 255 kí tự")
    private String url;

    @NotNull
    private boolean main;

    @NotNull
    @Length(max = 4000, message = "Ghi chú không được quá 4000 kí tự")
    private String note;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(UUID damtomId) {
        this.damtomId = damtomId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

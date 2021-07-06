package org.thingsboard.server.dft.controllers.web.damtom.dtos;

public class DeviceCreateDto {
    private String damtomId;
    private String name;
    private String note;
    private boolean active;

    public DeviceCreateDto() {
    }

    public String getDamtomId() {
        return damtomId;
    }

    public void setDamtomId(String damtomId) {
        this.damtomId = damtomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

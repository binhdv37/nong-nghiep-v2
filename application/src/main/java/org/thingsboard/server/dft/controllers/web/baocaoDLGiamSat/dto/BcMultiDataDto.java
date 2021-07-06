package org.thingsboard.server.dft.controllers.web.baocaoDLGiamSat.dto;

import java.util.List;

public class BcMultiDataDto {
    // 1 object nay tuong duong du lieu cua 1 key, 1 dam tom, 1 khoang time

    private String name;
    private List<SeriesDto> series;

    public BcMultiDataDto() {
    }

    public BcMultiDataDto(String name, List<SeriesDto> series) {
        this.name = name;
        this.series = series;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeriesDto> getSeries() {
        return series;
    }

    public void setSeries(List<SeriesDto> series) {
        this.series = series;
    }
}

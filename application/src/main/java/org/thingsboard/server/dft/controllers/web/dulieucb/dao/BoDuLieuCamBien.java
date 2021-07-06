package org.thingsboard.server.dft.controllers.web.dulieucb.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BoDuLieuCamBien {
    private String khoangThoiGian;
    private int span;
    private boolean display;
    private UUID getwayId;
    private String tenGateway;
    private Map<String, Double> data;

    public BoDuLieuCamBien(String khoangThoiGian, int span,
                           boolean display, UUID getwayId, String tenGateway, Map<String, Double> data) {
        this.khoangThoiGian = khoangThoiGian;
        this.span = span;
        this.display = display;
        this.getwayId = getwayId;
        this.tenGateway = tenGateway;
        this.data = data;
    }
}

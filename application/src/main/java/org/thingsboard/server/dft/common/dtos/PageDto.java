package org.thingsboard.server.dft.common.dtos;

import java.util.Collections;
import java.util.List;

public class PageDto<T> {

    private List<T> list;
    private int totalElements;

    public PageDto() {
        this.list = Collections.emptyList();
        this.totalElements = 0;
    }

    public PageDto(List<T> list) {
        if (list == null) {
            return;
        }
        this.list = list;
        this.totalElements = list.size();
    }

    public List<T> getList() {
        return list;
    }

    public void setList(
        List<T> list) {
        this.list = list;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }
}

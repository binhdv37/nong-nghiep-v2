package org.thingsboard.server.dft.common.dtos;

// binhdv
public class TimeRangeNameDto {
    private long startTs;
    private long endTs;
    private String name;

    public TimeRangeNameDto() {
    }

    public TimeRangeNameDto(long startTs, long endTs, String name) {
        this.startTs = startTs;
        this.endTs = endTs;
        this.name = name;
    }

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public long getEndTs() {
        return endTs;
    }

    public void setEndTs(long endTs) {
        this.endTs = endTs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package org.cri.redmetrics.model;


import java.util.Date;

public class BinCount {
    public BinCount(Date date, long count) {
        this.date = date;
        this.count = count;
    }

    public Date date;
    public long count;
}

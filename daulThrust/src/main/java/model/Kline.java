package model;

import java.math.BigDecimal;

public class Kline {
    private long time;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal volume;

    public Kline(long time, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice, BigDecimal volume) {
        this.time = time;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    public synchronized long getTime() {
        return time;
    }

    public synchronized void setTime(long time) {
        this.time = time;
    }

    public synchronized BigDecimal getOpenPrice() {
        return openPrice;
    }

    public synchronized void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public synchronized BigDecimal getHighPrice() {
        return highPrice;
    }

    public synchronized void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public synchronized BigDecimal getLowPrice() {
        return lowPrice;
    }

    public synchronized void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public synchronized BigDecimal getClosePrice() {
        return closePrice;
    }

    public synchronized void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public synchronized BigDecimal getVolume() {
        return volume;
    }

    public synchronized void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Kline{" +
                "volume=" + volume +
                ", closePrice=" + closePrice +
                ", lowPrice=" + lowPrice +
                ", highPrice=" + highPrice +
                ", openPrice=" + openPrice +
                ", time=" + time +
                '}';
    }
}

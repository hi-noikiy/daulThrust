package model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by tonyqi on 16-10-12.
 */
public class KLineList {
    private Kline firKline;
    private Kline secKline;
    private volatile BigDecimal lastPrice;

    private KLineList() {
    }

    public synchronized BigDecimal getLastPrice() {
        return lastPrice;
    }

    public synchronized void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public static KLineList getInstance() {
        return InnerClass.getKLineList();
    }

    //放入kline方法，只有kline交替才返回true
    public boolean setKline(Kline kline) {
        if (firKline == null || firKline.getTime() == kline.getTime()) {
            firKline = kline;
            return false;
        }
        if (secKline == null) {
            secKline = kline;
            return true;
        }
        if (secKline.getTime() == kline.getTime()) {
            secKline = kline;
            return false;
        }
        firKline = secKline;
        secKline = kline;
        return true;
    }

    public Kline getFirKline() {
        return firKline;
    }


    public Kline getSecKline() {
        return secKline;
    }

    private static class InnerClass {
        private static KLineList kLineList;

        static KLineList getKLineList() {
            if (kLineList == null)
                kLineList = new KLineList();
            return kLineList;
        }
    }

    @Override
    public String toString() {
        return "KLineList{" +
                "firKline=" + firKline +
                ", secKline=" + secKline +
                '}';
    }
}

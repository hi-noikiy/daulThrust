package model;

import java.math.BigDecimal;

/**
 * Created by tonyqi on 16-10-12.
 */
public class TradeOrder {

    private int id;//zizeng

    private long orderId;
    private BigDecimal openPrice;
    private BigDecimal avgPrice;
    private BigDecimal dealAmount;
    private BigDecimal earningP;
    private BigDecimal amount;
    private boolean isComplete;
    private boolean isTickOrder;//已经挂单
    private long createTime;
    private int minStrategy;//几分钟策略
    private boolean isCloseout;

    public long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getEarningP() {
        return earningP;
    }

    public void setEarningP(BigDecimal earningP) {
        this.earningP = earningP;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isTickOrder() {
        return isTickOrder;
    }

    public void setTickOrder(boolean tickOrder) {
        isTickOrder = tickOrder;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getMinStrategy() {
        return minStrategy;
    }

    public void setMinStrategy(int minStrategy) {
        this.minStrategy = minStrategy;
    }

    public boolean isCloseout() {
        return isCloseout;
    }

    public void setCloseout(boolean closeout) {
        isCloseout = closeout;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TradeOrder{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", openPrice=" + openPrice +
                ", avgPrice=" + avgPrice +
                ", dealAmount=" + dealAmount +
                ", earningP=" + earningP +
                ", amount=" + amount +
                ", isComplete=" + isComplete +
                ", isTickOrder=" + isTickOrder +
                ", createTime=" + createTime +
                ", minStrategy=" + minStrategy +
                ", isCloseout=" + isCloseout +
                '}';
    }
}

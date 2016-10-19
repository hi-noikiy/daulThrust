package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by tonyqi on 16-10-12.
 */
public class TradeOrder {

    private int id;//zizeng

    private long orderId;
    private BigDecimal tickPrice;
    private BigDecimal avgPrice;
    private BigDecimal amount;
    private BigDecimal dealAmount;
    private boolean isComplete;
    private boolean isCloseout;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getTickPrice() {
        return tickPrice;
    }

    public void setTickPrice(BigDecimal tickPrice) {
        this.tickPrice = tickPrice;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isCloseout() {
        return isCloseout;
    }

    public void setCloseout(boolean closeout) {
        isCloseout = closeout;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TradeOrder{\n" +
                "id=" + id + "\n" +
                ", orderId=" + orderId + "\n" +
                ", tickPrice=" + tickPrice + "\n" +
                ", avgPrice=" + avgPrice + "\n" +
                ", amount=" + amount + "\n" +
                ", dealAmount=" + dealAmount + "\n" +
                ", isComplete=" + isComplete + "\n" +
                ", isCloseout=" + isCloseout + "\n" +
                ", createTime=" + createTime + "\n" +
                "}\n";
    }
}

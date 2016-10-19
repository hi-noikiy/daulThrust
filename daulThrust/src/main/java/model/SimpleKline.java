package model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import webSocket.Example;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by tonyqi on 16-10-17.
 * 保存两根kline
 */
public class SimpleKline {
    Log log = LogFactory.getLog(SimpleKline.class);

    private static Kline firKline;

    private static Kline secKline;

    private static BigDecimal gains = BigDecimal.valueOf(200);

    private static BigDecimal allGains = BigDecimal.ZERO;

    private volatile TradeOrder order;

    private BigDecimal stopLossPrice = BigDecimal.valueOf(2);//止损金额10元

    private KLineList kLineList = KLineList.getInstance();

    private long startTime;

    /**
     * return true 表示需要判断
     * 需判断上一张订单是否盈利
     * 需判断能否开仓，与初始化数据
     *
     * @param kline
     * @return
     */
    public synchronized void setKline(Kline kline) {
        if (firKline == null)
            firKline = kline;
        else if (firKline.getTime() == kline.getTime())
            firKline = kline;
        else if (secKline == null && firKline.getTime() < kline.getTime()) {
            secKline = kline;
            checkCloseoutOrder();
            checkFirKline();
        } else if (secKline != null && secKline.getTime() == kline.getTime()) {
            secKline = kline;
        } else if (kline.getTime() > secKline.getTime()) {
            log.warn("第三根kline....");
            checkSecKline();
            initKline();
            log.warn("第三根结束,第一根第二根清空");
        }
    }

    private synchronized void checkFirKline() {
        if (firKline.getClosePrice().compareTo(firKline.getOpenPrice()) == -1) {
            log.info("firKline 不符合条件 " + firKline);
            initKline();
        } else {
            log.info("firKline 符合条件 " + firKline);
        }
    }

    private synchronized void checkSecKline() {
        if (secKline.getClosePrice().compareTo(secKline.getOpenPrice()) >= 0) {//调用策略下单
            log.info("secKline 符合条件 " + secKline);
            startTime = System.currentTimeMillis();
            order();
            System.out.println("下单完毕，初始化第一根和第二个呢，开始下一轮...");
        } else {
            log.info("secKline 不符合条件 " + secKline + " - 开始重新计算");
        }
    }

    //初始化
    private synchronized void initKline() {
        firKline = null;
        secKline = null;
    }


    private synchronized void checkCloseoutOrder() {
        log.info("开始" + Example.minStrategy + "min策略");
        if (order != null) {//有开仓，kline肯定被初始化过，到现在这个点，是有两根kline的时间
            log.info("检测订单是否盈利,,tickPrice = " + order.getTickPrice() + ",orderId = " + order.getOrderId() + ",amount = " + order.getAmount());
            BigDecimal price = order.getTickPrice();
            BigDecimal highP = firKline.getHighPrice();
            if (highP.compareTo(price) >= 0) {
                log.info("单个币盈利:" + gains + ",截至目前总盈利:" + (allGains = allGains.add(gains)));
            } else {
                BigDecimal lastPrice = KLineList.getInstance().getLastPrice();
                BigDecimal noGains = lastPrice.subtract(price.subtract(gains));
                String msg = "亏损";
                if (noGains.compareTo(BigDecimal.ZERO) >= 0) {
                    msg = "盈利";
                }
                log.info("未达到预期价格,按照此时价格平仓,此时lastPrice=" + lastPrice + "," + msg + "：" + noGains + ",总盈利:" + (allGains = allGains.add(noGains)));
            }
        } else {
            log.info("上笔无成交单,或已被平仓");
        }
        order = null;
    }

    private synchronized void order() {
        TradeOrder order = createOrder(false, getAmount(), null);//create order
        log.info("trade open order ,tickPrice = " + order.getTickPrice() + ",orderId = " + order.getOrderId() + ",amount = " + order.getAmount());
        this.order = createOrder(true, getAmount(), order.getTickPrice());//create closeoutOrder
        log.info("trade close order ,tickPrice = " + this.order.getTickPrice() + ",orderId = " + this.order.getOrderId() + ",amount = " + this.order.getAmount());
        new CloseoutOrderMonitor().run();
//            apiResult.getTradeRet(apiKey, secretKey, "", String.valueOf(order.getTickPrice()), String.valueOf(order.getAmount()), "");//test

    }

    private BigDecimal getAmount() {
        return BigDecimal.ONE;
    }

    private synchronized static TradeOrder createOrder(boolean isCloseout, BigDecimal amount, BigDecimal price) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setCloseout(isCloseout);
        tradeOrder.setAmount(amount);
        if (price == null)
            price = KLineList.getInstance().getLastPrice();
        if (isCloseout) {
            tradeOrder.setTickPrice((price.add(gains)).setScale(BigDecimal.ROUND_DOWN, 2));
        } else {
            tradeOrder.setTickPrice(price.setScale(BigDecimal.ROUND_DOWN, 2));//// TODO: 16-10-17 正式环境需要add gains,假设秒撮合，正式环境需要检测
        }
        tradeOrder.setCreateTime(new Date());
        return tradeOrder;
    }

//    private static String getTime() {
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//    }

    private class CloseoutOrderMonitor extends Thread {
        public void run() {
            long time = 1000 * 60 * Example.minStrategy;
            log.info("开始检测价格，进行止损，检测订单:" + "tickPrice = " + order.getTickPrice() + ",orderId = " + order.getOrderId() + ",amount = " + order.getAmount());
            BigDecimal lowPrice = order.getTickPrice().subtract(gains).subtract(stopLossPrice);//挂单价-盈利-止损金额
            boolean flag = true;
            while (flag) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BigDecimal lastP = kLineList.getLastPrice();
                if (lowPrice.compareTo(lastP) >= 0) {//止损线
                    log.info("价格跌到止损线，目前价格为：+" + lastP + ",执行强制平仓，总盈利减去止损金额" + stopLossPrice + ",目前总盈利:" + (allGains = allGains.subtract(stopLossPrice)));
                    order = null;
                    flag = false;
                }

                if (System.currentTimeMillis() >= (startTime + time)) {
                    log.info("监控时间为" + time / 1000 + "s,超时结束监控");
                    flag = false;
                }

            }
            initKline();
        }
    }

}

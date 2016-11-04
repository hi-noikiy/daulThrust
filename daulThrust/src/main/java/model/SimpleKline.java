package model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import strategy.ApiResult;
import webSocket.Example;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by tonyqi on 16-10-17.
 * 保存两根kline
 */
public class SimpleKline {
    private Log log = LogFactory.getLog(SimpleKline.class);

    private static Kline firKline;

    private static Kline secKline;

    private static Kline thiKline;

    private static BigDecimal gains = BigDecimal.valueOf(4);

    private static BigDecimal allGains = BigDecimal.ZERO;

    private volatile TradeOrder order;

    private static BigDecimal stopLossPrice = BigDecimal.valueOf(Example.minStrategy * 3);//止损金额 分时*2

    private KLineList kLineList = KLineList.getInstance();

    private static BigDecimal buyPrice;//最初买入价

    private long startTime;

    private String apiKey = "d00c60fb-1ad7-4515-b103-66e8949eac99";
    private String secretKey = "0D6676D62212297F179CB00A1E561008";
    private String symbol = "btc_cny";

    private Long secStartT;
    private boolean open;

    /**
     * return true 表示需要判断
     * 需判断上一张订单是否盈利
     * 需判断能否开仓，与初始化数据
     *
     * @param kline
     * @return
     */
    public synchronized void setKline(Kline kline) {
        if (firKline == null) {
            firKline = kline;
            checkCloseoutOrder();
            order = null;
        } else if (firKline.getTime() == kline.getTime())
            firKline = kline;
        else if (secKline == null && firKline.getTime() < kline.getTime()) {
            secKline = kline;
            checkFirKline();
        } else if (secKline != null && secKline.getTime() == kline.getTime()) {
            secKline = kline;
            checkSecKline();
        } else if (secKline != null && thiKline == null && secKline.getTime() < kline.getTime()) {
            thiKline = kline;
        } else if (thiKline != null && thiKline.getTime() == kline.getTime())
            thiKline = kline;
        else if (thiKline != null && thiKline.getTime() < kline.getTime()) {
            initKline();
        } else {
            log.error("what's wrong　kline ＝　" + kline);
        }
    }

    private synchronized void checkFirKline() {
        open = false;
        if (firKline.getClosePrice().compareTo(firKline.getOpenPrice()) == -1)
            initKline();
        else
            secStartT = System.currentTimeMillis();
    }

    private synchronized void checkSecKline() {
        if (open)
            return;
        if (System.currentTimeMillis() - secStartT >= Example.minStrategy * 60000 - 2000) {//提前两秒
            if (secKline.getClosePrice().compareTo(secKline.getOpenPrice()) >= 0) {//调用策略下单
                open = true;
                startTime = System.currentTimeMillis();
                order();
            } else {
                initKline();
            }
        }
    }

    //初始化
    private synchronized void initKline() {
        firKline = null;
        secKline = null;
        thiKline = null;
    }


    private synchronized void checkCloseoutOrder() {//检查平仓订单，计算上笔订单盈利
        if (order != null) {
            long orderId = order.getOrderId();
            //取消订单
            ApiResult.getCancelOrderRet(apiKey, secretKey, symbol, orderId);
            //获取订单详情
            ApiResult.OrderInfo orderInfo = ApiResult.getOrderInfoRet(apiKey, secretKey, symbol, String.valueOf(orderId));
            BigDecimal dealAmount = orderInfo.getDealAmount();
            BigDecimal avgPrice = orderInfo.getAvgPrice();
            BigDecimal tickPrice = orderInfo.getPrice();
            BigDecimal tickAmount = orderInfo.getAmount();
            if (dealAmount.compareTo(tickAmount) == -1) {//成交量小于挂单量
                BigDecimal initGains = (avgPrice.subtract(buyPrice)).multiply(dealAmount);
                closeout(tickAmount.subtract(dealAmount).setScale(2, BigDecimal.ROUND_DOWN), initGains);
            } else {//完全成交－平仓单价格是开仓单的平均成交价加上gains
                BigDecimal gain = (gains).multiply(tickAmount);
                log.info("此次交易盈利:" + gain + ",截至目前总盈利:" + (allGains = allGains.add(gain)));
            }
        } else {
            log.info("上笔无开单");
        }
    }

    private void closeout(BigDecimal amount, BigDecimal initGains) {////需要强制平仓的数量，保留两位

        if (amount.compareTo(BigDecimal.ZERO) != 1) {//数量小于等于０
            log.info("此次交易盈利: " + initGains + ", 截至目前总盈利:" + (allGains = allGains.add(initGains)));
            return;
        }

        checkUserInfo(amount);

//        BigDecimal p = kLineList.getLastPrice().subtract(stopLossPrice.multiply(BigDecimal.valueOf(2)));//强制平仓挂单价
        ApiResult.Trade trade = ApiResult.getTradeRet(apiKey, secretKey, symbol, null, String.valueOf(amount), "sell_market");
        if (trade == null) {
            log.error("trade error.....amount = " + amount + ",price = 市价");//todo 遇到这种问题。后续强平
            return;
        }
        new CheckOrder(initGains, trade.getOrderId()).start();
    }

    private synchronized void order() {
        TradeOrder order = createOrder(false, getAmount(), null);//create order
        ApiResult.Trade trade = ApiResult.getTradeRet(apiKey, secretKey, symbol, String.valueOf(order.getTickPrice()), String.valueOf(order.getAmount()), "buy");
        if (trade == null) {
            log.error("trade error.....amount = " + order.getAmount() + ",price = " + order.getTickPrice());//todo 遇到这种问题。后续强平
            return;
        }
//        ApiResult.sleep(1000);
        ApiResult.OrderInfo orderInfo;
        while (true) {//循环获取成交
            orderInfo = ApiResult.getOrderInfoRet(apiKey, secretKey, symbol, String.valueOf(trade.getOrderId()));
            if (orderInfo != null && orderInfo.getDealAmount().compareTo(orderInfo.getAmount()) == 0)
                break;
            ApiResult.sleep(200);
        }
        buyPrice = orderInfo.getAvgPrice();//买入价格，为统计盈利
        log.info("trade open order ,tickPrice = " + order.getTickPrice() + " , amount = " + order.getAmount() + ", avgPrice = " + buyPrice);
        BigDecimal amount = orderInfo.getAmount();
        this.order = createOrder(true, amount, orderInfo.getAvgPrice());//create closeoutOrder

        checkUserInfo(amount);

        ApiResult.Trade t = ApiResult.getTradeRet(apiKey, secretKey, symbol, String.valueOf(this.order.getTickPrice()), String.valueOf(this.order.getAmount()), "sell");
        this.order.setOrderId(t.getOrderId());
        log.info("trade close order ,tickPrice = " + this.order.getTickPrice() + ",amount = " + this.order.getAmount());
        new CloseoutOrderMonitor().start();//检测订单是否止损
    }

    private boolean checkUserInfo(BigDecimal amount) {
        ApiResult.UserInfo userInfo;
        int count = 0;
        while (true) {
            userInfo = ApiResult.getUserInfoRet(apiKey, secretKey);
            if (userInfo.getBtc().compareTo(amount) >= 0)
                break;
            count++;
            if (count >= 100) {
                log.error("异常。挂卖单账户持币不足...amount = " + amount);
                ApiResult.exit();
            }
            ApiResult.sleep(50);
        }
        return true;
    }

    private BigDecimal getAmount() {
        return BigDecimal.valueOf(1);
    }

    private synchronized static TradeOrder createOrder(boolean isCloseout, BigDecimal amount, BigDecimal price) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setCloseout(isCloseout);
        tradeOrder.setAmount(amount);
        if (price == null)
            price = KLineList.getInstance().getLastPrice();
        if (isCloseout) {
            tradeOrder.setTickPrice((price.add(gains)).setScale(2, BigDecimal.ROUND_DOWN));
        } else {
            tradeOrder.setTickPrice(price.add(stopLossPrice).setScale(2, BigDecimal.ROUND_DOWN));
        }
        tradeOrder.setCreateTime(new Date());
        return tradeOrder;
    }

    private class CloseoutOrderMonitor extends Thread {//检测价格是否达到止损价。如果不需要止损，只需要注释掉此方法的调用

        public void run() {
            long time = 1000 * 60 * Example.minStrategy;
//            log.info("开始检测价格，进行止损，检测订单:" + "tickPrice = " + order.getTickPrice() + ",orderId = " + order.getOrderId() + ",amount = " + order.getAmount());
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
//                    log.info("价格跌到止损线，目前价格为：+" + lastP + ",执行强制平仓，总盈利减去止损金额" + stopLossPrice + ",目前总盈利:" + (allGains = allGains.subtract(stopLossPrice)));
                    log.warn("《《《《《《《《《《《《《《《《《《《《《《《《《《价格跌到止损线。开始执行止损》》》》》》》》》》》》》》》》》》》》》》》》》》");
                    checkCloseoutOrder();
                    order = null;
                    flag = false;
                }

                if (System.currentTimeMillis() >= (startTime + time)) {
//                    log.info("监控时间为" + time / 1000 + "s,超时结束监控");
                    flag = false;
                }

            }
        }
    }

    private class CheckOrder extends Thread {
        private BigDecimal initGains;//平仓单自动成交的盈利
        private long orderId;

        CheckOrder(BigDecimal initGains, long orderId) {
            this.initGains = initGains;
            this.orderId = orderId;
        }

        @Override
        public void run() {
            ApiResult.sleep(200);
            ApiResult.OrderInfo o = ApiResult.getOrderInfoRet(apiKey, secretKey, symbol, String.valueOf(orderId));
            BigDecimal gains = initGains.add((o.getAvgPrice().subtract(buyPrice)).multiply(o.getAmount()));
            String msg = "盈利";
            if (gains.compareTo(BigDecimal.ZERO) == -1) {
                msg = "亏损";
            }
            log.info("此次交易" + msg + " : " + gains + ", 截至目前总盈利:" + (allGains = allGains.add(gains)));
        }
    }
}

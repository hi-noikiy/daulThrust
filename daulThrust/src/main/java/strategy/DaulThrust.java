package strategy;

import model.KLineList;
import model.Kline;
import model.TradeOrder;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * a = 最高价　- 收盘价
 * b = 收盘价　- 最低价
 * c = a | b　的最大值
 * k = 0.7
 * 触发值 = c * k
 * ----------以上为第一根k线------------
 * a = 开盘价
 * 价格超过 a + 触发 ＝　做多
 * 价格低于 a + 触发 = 做空
 */
public class DaulThrust {
    private TradeOrder closeoutOrder;
    private TradeOrder openOrder;

    private long mustEndTime;//must end time


    private BigDecimal earning = BigDecimal.valueOf(3);//盈利
    private KLineList kLineList = KLineList.getInstance();
    //    public static CopyOnWriteArrayList<TradeOrder> orders = new CopyOnWriteArrayList<TradeOrder>();
    private BigDecimal k = BigDecimal.valueOf(0.7);//k

    private BigDecimal totalEarning = BigDecimal.ZERO;

    private BigDecimal percent = BigDecimal.valueOf(0.2);

    private ApiResult apiResult = ApiResult.getInstance();

    private String apiKey = "";
    private String secretKey = "";

    private Integer id = 0;


    public void order() {
        mustEndTime = System.currentTimeMillis() + 1350000;//private long strategyMin = 1350000;//分时策略的4分之3 = 1000 * 60 * 30 * 3 / 4
        checkCloseoutOrder();
        checkOrder();//check if you can order
    }

    private void checkOrder() {
        System.out.println("check order...");
        Kline fir = kLineList.getFirKline();//firstKline
        Kline sec = kLineList.getSecKline();//secondKline
        BigDecimal a = fir.getHighPrice().subtract(fir.getClosePrice());//firstKline.highPrice - firstKline.closePrice
        BigDecimal b = fir.getClosePrice().subtract(fir.getLowPrice());//firstKline.closePrice - firstKline.lowPrice
        BigDecimal c = a.subtract(b).compareTo(BigDecimal.ZERO) == 1 ? a : b;//choose the max value on a or b
        if (c.compareTo(BigDecimal.ZERO) == 0) {//if c = 0
            System.out.println("can't order ,touchValue = 0");
            return;
        }
        BigDecimal d = c.multiply(k);//c * k

        System.out.println("a:" + a + ",b:" + b + ",c:" + c + "d:" + d);

        BigDecimal secOpenPrice = sec.getOpenPrice();//secondKline.openPrice
        BigDecimal openPrice = d.add(secOpenPrice);//open price

        System.out.println("open price is " + openPrice + " and the last price is " + kLineList.getLastPrice() + " now ! the time is " + getTime());
        System.out.println("now kline is " + kLineList.getSecKline());

//        lastPriceLoop(openPrice);//loop the last price
    }

    private TradeOrder createOrder(boolean isCloseout, BigDecimal openPrice, BigDecimal amount) {
        return new TradeOrder();
    }

    private void lastPriceLoop(BigDecimal openPrice) {

        sleep(500);

        if (System.currentTimeMillis() > mustEndTime) {
            System.out.println("timeout in loop the last price\n" +
                    "the second kline is [" + kLineList.getSecKline() + "]\n" +
                    "open price is " + openPrice
                    + "time is " + getTime()
            );
            return;
        }
        BigDecimal lastPrice = kLineList.getLastPrice();
        if (lastPrice.compareTo(openPrice) >= 0) {//buy-last price >= open price
            openOrder = createOrder(false, lastPrice.add(percent).setScale(BigDecimal.ROUND_DOWN, 2), getAmount());


//            if (tradeOrder(openOrder)) {//trade order success // TODO: 16-10-14 test
//                loopComplete(openOrder);
//            }


            System.out.println("trade openorder " + openOrder);
        } else {//sell
            ////////////////////////now just buy
            lastPriceLoop(openPrice);
        }
    }

    private void loopComplete(TradeOrder order) {
        sleep(1000);

        if (System.currentTimeMillis() > mustEndTime) {
            System.out.println("timeout in loop the open order complete\n" +
                    "order id is [" + openOrder.getOrderId() + "]\n"
                    + "time is " + getTime()
            );
            closeoutOrder(order);//compel closeout
            return;
        }
        if (checkComplete(order)) {//complete
            System.out.println("open order is complete ! order id is [" + openOrder.getOrderId() + "]\n" +
                    "time is " + getTime());
            tradeOrder(createOrder(true, order.getTickPrice(), order.getAmount()));//trade closeout order
        }
        loopComplete(order);
    }

    private boolean tradeOrder(TradeOrder openOrder) {
        ApiResult.Trade trade = apiResult.getTradeRet(apiKey, secretKey, "btc_cny", String.valueOf(openOrder.getTickPrice()), String.valueOf(openOrder.getAmount()), "buy");
        if (trade.getResult().equals("true")) {
            openOrder.setOrderId(trade.getOrderId());
//            saveOrder(openOrder);//TODO
            return true;
        } else
            return false;
    }

    private void checkCloseoutOrder() {
        System.out.println("check closeout order");
        if (closeoutOrder == null) {
            System.out.println("The previous closeoutOrder is null ! " + "time is " + getTime());
            return;
        }
        if (checkComplete(closeoutOrder)) {//check order complete
            System.out.println("closeout order is complete ! order id is " + closeoutOrder.getOrderId() + "time is " + getTime());
        } else {
            closeoutOrder(closeoutOrder);
        }
    }

    private boolean checkComplete(TradeOrder order) {
        ApiResult.OrderInfo orderInfo = apiResult.getOrderInfoRet(apiKey, secretKey, "btc_cny", String.valueOf(order.getOrderId()));
        return orderInfo.result.equals("true") && orderInfo.getDealAmount().compareTo(orderInfo.getAmount()) >= 0;
    }

    private static void sleep(long timestamp) {
        try {
            Thread.sleep(timestamp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private BigDecimal getAmount() {
        return apiResult.getUserInfoRet(apiKey, secretKey).getBtc().setScale(BigDecimal.ROUND_DOWN, 2);
    }

    private void closeoutOrder(TradeOrder order) {
        apiResult.getCancelOrderRet(apiKey, secretKey, "btc_cny", order.getOrderId());
        sleep(300);//对面接口延迟
        ApiResult.OrderInfo orderInfo = apiResult.getOrderInfoRet(apiKey, secretKey, "btc_cny", String.valueOf(order.getOrderId()));
        BigDecimal price = kLineList.getLastPrice().add(BigDecimal.valueOf(3));
        BigDecimal amount = orderInfo.getAmount().subtract(orderInfo.getDealAmount());
        ApiResult.Trade trade = apiResult.getTradeRet(apiKey, secretKey, "btc_cny", String.valueOf(price), String.valueOf(amount), "sell");
        TradeOrder closeOrder = createOrder(true, price, amount);
        if (trade.getResult().equals("true"))
            closeOrder.setOrderId(trade.getOrderId());
//        else
//            closeOrder.setTickOrder(false);
        this.closeoutOrder = closeOrder;
        System.out.println("closeout Order:" + closeOrder);
        //todo save;
    }

    private String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}

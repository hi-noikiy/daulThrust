package arbitrage;


import huobi.IHuobiRestApi;
import huobi.impl.HuobiRestApi;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import strategy.ApiResult;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tonyqi on 16-12-20.
 */
public class SavePriceMargin {
    private static Long lastTime = System.currentTimeMillis();

    private static IHuobiRestApi huobiRestApi = new HuobiRestApi();

    public static BigDecimal priceMargin = BigDecimal.ONE;//程序启动时赋值

    public static BigDecimal tradeAmount = BigDecimal.ONE;//程序启动后检测完账号持币情况后赋值

    private static String priceM;//比较价差 okcoin价高且价差大于priceMargin为1 火币大为-1  交易前赋值

    public static String hasCoin = "0";//启动时检查账户并赋值,1为okcoin持币 0 为都不持币 -1为火币网持币 交易后赋值

    private static Log log = LogFactory.getLog(SavePriceMargin.class);

    private static String a = "1";

    private static String b = "-1";

    private static Lock lock = new ReentrantLock(true);

    private static String okApiKey = "";

    private static String okSecretKey = "";

    private static String hbApiKey = "";

    private static String hbSecretKey = "";

    private static ExecutorService cachedThreadPool = Executors.newSingleThreadExecutor();

    //比较价格,3s比较一次
    public static void startComparePrice(BigDecimal okcoinPrice) {
        Long now;
        if ((now = System.currentTimeMillis()) - lastTime < 3000)
            return;
        lastTime = now;
        lock.lock();
        try {
            BigDecimal huobiPrice = huobiRestApi.ticker();//获取火币lastPrice
            if (huobiPrice == null)
                return;
            BigDecimal pm = okcoinPrice.subtract(huobiPrice);//差价
            if (pm.compareTo(BigDecimal.ZERO) >= 0) {//价差大于等于0
                if (pm.compareTo(priceMargin) <= 0)//小于等于价差,不满足搬砖条件
                    return;
                /**-----------搬砖-----------*/
                priceM = a;
                trade(okcoinPrice, huobiPrice);
            } else {//价差小于0
                BigDecimal temp = pm.subtract(pm.multiply(BigDecimal.valueOf(2)));//价差-2*价差
                if (temp.compareTo(priceMargin) <= 0)//小于等于价差,不满足搬砖条件
                    return;
                /**-----------搬砖-----------*/
                priceM = b;
                trade(okcoinPrice, huobiPrice);
            }
        } finally {
            lock.unlock();
        }
    }

    private static BigDecimal gains = BigDecimal.ZERO;//总盈利

    /**
     * 搬砖套利
     */
    private static void trade(BigDecimal okPrice, BigDecimal hbPrice) {
        if (priceM.equals(hasCoin)) {//符合交易条件
            String amount = String.valueOf(tradeAmount);
            long okTID = 0, hbTID = 0;
            String hbP, okP;
            /**------okcoin卖 huobi买-------*/
            if (priceM.equals(a)) {//1-okcoin价高
                String result = huobiRestApi.trade(hbApiKey, hbSecretKey, "btc_cny", hbP = getMuchBigPrice(hbPrice), amount, "buy");//huobi通过rest接口下单
                ApiResult.Trade huobiTrade = jsonHandle(result);
                if (huobiTrade.getResult().equals("false")) {
                    log.error("程序异常!火币接口下单失败,方向buy,单价" + hbP + ",订单数量" + amount);
                    System.exit(0);
                }
                hbTID = huobiTrade.getOrderId();
                ApiResult.Trade trade = ApiResult.getTradeRet(okApiKey, okSecretKey, "btc_cny", okP = getMuchSmallPrice(okPrice), amount, "sell");//okcoin通过webSocket下单
                if (trade == null) {
                    log.error("程序异常!OKCOIN接口下单失败,方向sell,单价" + okP + ",订单数量" + amount);
                    System.exit(0);
                }
                hasCoin = b;
                okTID = trade.getOrderId();

            }
            /**------okcoin买 huobi卖-------*/
            else {//-1huobi价高
                String result = huobiRestApi.trade(hbApiKey, hbSecretKey, "btc_cny", hbP = getMuchSmallPrice(hbPrice), amount, "sell");
                ApiResult.Trade huobiTrade = jsonHandle(result);
                if (huobiTrade.getResult().equals("false")) {
                    log.error("程序异常!火币接口下单失败,方向sell,单价" + hbP + ",订单数量" + amount);
                    System.exit(0);
                }
                ApiResult.Trade trade = ApiResult.getTradeRet(okApiKey, okSecretKey, "btc_cny", okP = getMuchBigPrice(okPrice), amount, "buy");
                if (trade == null) {
                    log.error("程序异常!OKCOIN接口下单失败,方向buy,单价" + okP + ",订单数量" + amount);
                    System.exit(0);
                }
                hasCoin = a;
            }
            reckonGains(okTID, hbTID, okPrice, hbPrice, priceM);
        } else {
            log.debug("*****Trade failed okPrice = " + okPrice + " , hbPrice = " + hbPrice + " , hasCoin = " + hasCoin + "*****");
        }
    }

    private static void reckonGains(long okTID, long hbTID, BigDecimal okPrice, BigDecimal hbPrice, String priceM) {
        cachedThreadPool.execute(new ReckonGains(okTID, hbTID, okPrice, hbPrice, priceM));
    }

    private static class ReckonGains implements Runnable {
        private long okTID;
        private long hbTID;
        private BigDecimal okPrice;
        private BigDecimal hbPrice;
        private String priceM;

        ReckonGains(long okTID, long hbTID, BigDecimal okPrice, BigDecimal hbPrice, String priceM) {
            this.okTID = okTID;
            this.hbTID = hbTID;
            this.okPrice = okPrice;
            this.hbPrice = hbPrice;
            this.priceM = priceM;
        }

        @Override
        public void run() {
            ApiResult.OrderInfo okOrderInfo = ApiResult.getOrderInfoRet(okApiKey, okSecretKey, "btc_cny", String.valueOf(okTID));
            String ret = huobiRestApi.orderInfo(hbApiKey, hbSecretKey, "btc_cny", String.valueOf(hbTID));
            ApiResult.OrderInfo hbOrderInfo = handleOrderInfo(ret);
            BigDecimal okAvgPrice = okOrderInfo.getAvgPrice();
            BigDecimal hbAvgPrice = hbOrderInfo.getAvgPrice();
            BigDecimal expectGains, realGains;
            if (priceM.equals(a)) {
                expectGains = tradeAmount.multiply(okPrice.subtract(hbPrice));
                realGains = tradeAmount.multiply(okAvgPrice.subtract(hbAvgPrice));
            } else {
                expectGains = tradeAmount.multiply(hbPrice.subtract(okPrice));
                realGains = tradeAmount.multiply(hbAvgPrice.subtract(okAvgPrice));
            }
            log.warn("搬砖完成--起始价格--okcoin:" + okPrice + ",huobi:" + hbPrice + "--实际成交价--okcoin:" + okAvgPrice + ",huobi:" + hbAvgPrice);
            log.warn("==========预计盈利:" + expectGains + " , 实际盈利:" + realGains + "==========");
        }

        private ApiResult.OrderInfo handleOrderInfo(String ret) {//// TODO: 16-12-22
            return new ApiResult.OrderInfo();
        }
    }

    private static String getMuchSmallPrice(BigDecimal price) {
        return String.valueOf(price.multiply(BigDecimal.valueOf(0.9)).setScale(2, BigDecimal.ROUND_DOWN));
    }

    private static String getMuchBigPrice(BigDecimal price) {
        return String.valueOf(price.multiply(BigDecimal.valueOf(1.1)).setScale(2, BigDecimal.ROUND_DOWN));
    }

    private static ApiResult.Trade jsonHandle(String huobiTradeRet) {//// TODO: 16-12-22
        return new ApiResult.Trade(1L, "");
    }
}

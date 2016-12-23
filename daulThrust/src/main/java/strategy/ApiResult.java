package strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import webSocket.Example;
import webSocket.WebSoketClient;

import java.math.BigDecimal;

/**
 * Created by tonyqi on 16-10-14.
 */
public class ApiResult {

//    中国站 apiKey:  8a134f3f-51b8-4993-a916-bad635fdaf15
//    中国站 secretKey:  D67E022D0242FC3FE737C78BF8B546E4

    private static String apiKey = "8a134f3f-51b8-4993-a916-bad635fdaf15";
    private static String secretKey = "D67E022D0242FC3FE737C78BF8B546E4";

    private ApiResult() {
    }

    private static class InnerClass {
        private static ApiResult result;

        static ApiResult getInstance() {
            if (result == null)
                result = new ApiResult();
            return result;
        }
    }

    public static ApiResult getInstance() {
        return InnerClass.getInstance();
    }

    private volatile static UserInfo userInfo = null;
    private volatile static Trade trade = null;
    private volatile static CancelOrder cancelOrder = null;
    private volatile static OrderInfo orderInfo = null;
    public static final String spot_cny_trade = "ok_spotcny_trade";
    public static final String spot_cny_cancel = "ok_spotcny_cancel_order";
    public static final String spot_cny_userInfo = "ok_spotcny_userinfo";
    public static final String spot_cny_orderInfo = "ok_spotcny_orderinfo";
    private static Log log = LogFactory.getLog(ApiResult.class);


    public static synchronized void setMsg(String msg) {
        JSONObject object = JSON.parseArray(msg).getJSONObject(0);//channel and data
        String s = object.getString("channel");
        JSONObject array = object.getJSONObject("data");
        /**
         * 交易返回信息json解析
         */
        if (s.equals(spot_cny_trade)) {
            String result = array.getString("result");
            if ("true".equals(result))
                trade = new Trade(array.getLong("order_id"), result);
            else
                log.error("trade error msg :" + msg);
        } else if (s.equals(spot_cny_cancel)) {
            String result = array.getString("result");
            if ("true".equals(result))
                cancelOrder = new CancelOrder(array.getLong("order_id"), result);
            else
                log.error("cancel order error msg :" + msg);
        } else if (s.equals(spot_cny_userInfo)) {
            if ("true".equals(array.getString("result"))) {
                array = array.getJSONObject("info").getJSONObject("funds").getJSONObject("free");
                userInfo = new UserInfo(handler(array.getBigDecimal("btc")), handler(array.getBigDecimal("cny")), handler(array.getBigDecimal("ltc")));
            } else
                log.error("getUserInfo error msg :" + msg);

            /**
             *　订单详情返回信息json解析
             */

        } else if (s.equals(spot_cny_orderInfo)) {
            String result = array.getString("result");
            if ("true".equals(result)) {
                JSONArray objects = array.getJSONArray("orders");
                if (objects == null || objects.size() < 1)//为true的情况orders也会为空
                    log.error("order info error msg :" + msg);
                else {//"amount" 0, "avg_price" 1, "deal_amount" 3
                    JSONObject o = objects.getJSONObject(0);
                    orderInfo = new OrderInfo(handler(o.getBigDecimal("amount")), handler(o.getBigDecimal("avg_price")), handler(o.getBigDecimal("deal_amount")), handler(o.getBigDecimal("price")), result);
                }
            } else
                log.error("order info error msg :" + msg);
        } else {
            log.error("without this operation :[" + msg + "]");

        }
        ApiResult.class.notify();
    }

    public static void exit() {
        log.error("程序异常，结束程序");
        System.exit(0);
    }

    private static BigDecimal handler(BigDecimal value) {
        return value.setScale(4, BigDecimal.ROUND_DOWN);
    }

    public static synchronized UserInfo getUserInfoRet(String apiKey, String secretKey) {
        userInfo = null;//重置
        Example.client.getUserInfo(apiKey, secretKey);//请求
        if (userInfo == null)
            waitApi(5000);//锁等待,等待另一个线程推送数据，5s超时
        return userInfo;
    }

    public static synchronized Trade getTradeRet(String apiKey, String secretKey, String symbol,
                                                 String price, String amount, String type) {
        trade = null;
        Example.client.spotTrade(apiKey, secretKey, symbol, price, amount, type);
        if (trade == null)
            waitApi(5000);
        return trade;
    }

    public static synchronized CancelOrder getCancelOrderRet(String apiKey, String secretKey, String symbol,
                                                             Long orderId) {
        cancelOrder = null;
        Example.client.cancelOrder(apiKey, secretKey, symbol, orderId);
        if (cancelOrder == null)
            waitApi(5000);
        return cancelOrder;
    }

    public static void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized OrderInfo getOrderInfoRet(String apiKey, String secretKey, String symbol, String orderId) {
        orderInfo = null;
        Example.client.orderInfo(apiKey, secretKey, symbol, orderId);
        if (orderInfo == null)
            waitApi(5000);
        return orderInfo;
    }

    private static void waitApi(long timeout) {
        try {
            ApiResult.class.wait(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class UserInfo {//free

        UserInfo(BigDecimal btc, BigDecimal cny, BigDecimal ltc) {
            this.btc = btc;
            this.cny = cny;
            this.ltc = ltc;
        }

        BigDecimal btc;
        BigDecimal cny;
        BigDecimal ltc;

        public BigDecimal getBtc() {
            return btc;
        }

        public void setBtc(BigDecimal btc) {
            this.btc = btc;
        }

        public BigDecimal getCny() {
            return cny;
        }

        public void setCny(BigDecimal cny) {
            this.cny = cny;
        }

        public BigDecimal getLtc() {
            return ltc;
        }

        public void setLtc(BigDecimal ltc) {
            this.ltc = ltc;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "btc=" + btc +
                    ", cny=" + cny +
                    ", ltc=" + ltc +
                    '}';
        }
    }

    public static class Trade {
        public Trade(long orderId, String result) {
            this.orderId = orderId;
            this.result = result;
        }

        long orderId;
        String result;

        public long getOrderId() {
            return orderId;
        }

        public void setOrderId(long orderId) {
            this.orderId = orderId;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "Trade{" +
                    "result='" + result + '\'' +
                    ", orderId=" + orderId +
                    '}';
        }
    }

    public static class OrderInfo {
        public OrderInfo() {
        }

        OrderInfo(BigDecimal amount, BigDecimal avgPrice, BigDecimal dealAmount, BigDecimal price, String result) {
            this.amount = amount;
            this.avgPrice = avgPrice;
            this.dealAmount = dealAmount;
            this.result = result;
            this.price = price;
        }

        BigDecimal amount;
        BigDecimal avgPrice;
        BigDecimal dealAmount;
        String result;
        BigDecimal price;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
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

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        @Override
        public String toString() {
            return "OrderInfo{" +
                    "amount=" + amount +
                    ", avgPrice=" + avgPrice +
                    ", dealAmount=" + dealAmount +
                    ", result='" + result + '\'' +
                    ", price=" + price +
                    '}';
        }
    }

    public static class CancelOrder {
        CancelOrder(long orderId, String result) {
            this.orderId = orderId;
            this.result = result;
        }

        long orderId;
        String result;

        public long getOrderId() {
            return orderId;
        }

        public void setOrderId(long orderId) {
            this.orderId = orderId;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "CancelOrder{" +
                    "result='" + result + '\'' +
                    ", orderId=" + orderId +
                    '}';
        }
    }
}

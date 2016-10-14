package strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import webSocket.Example;
import webSocket.WebSoketClient;

import java.math.BigDecimal;

/**
 * Created by tonyqi on 16-10-14.
 */
public class ApiResult {
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

    private static WebSoketClient client = Example.client;
    private static UserInfo userInfo = null;
    private static Trade trade = null;
    private static CancelOrder cancelOrder = null;
    private static OrderInfo orderInfo = null;
    //    private static final String spot_cny_kline_30min = "ok_sub_spotcny_btc_kline_30min";
    private static final String spot_cny_trade = "ok_spotcny_trade";
    private static final String spot_cny_cancel = "ok_spotcny_cancel_order";
    private static final String spot_cny_userInfo = "ok_spotcny_userinfo";
    private static final String spot_cny_orderInfo = "ok_spotcny_orderinfo";

    public synchronized void setMsg(String msg) {
        JSONObject object = JSON.parseArray(msg).getJSONObject(0);//channel and data
        String s = String.valueOf(object.get("channel"));
        JSONObject array = object.getJSONObject("data");
        if (s.equals(spot_cny_trade)) {
            trade = new Trade(array.getLong("order_id"), array.getString("result"));
        } else if (s.equals(spot_cny_cancel)) {
            cancelOrder = new CancelOrder(array.getLong("order_id"), array.getString("result"));
        } else if (s.equals(spot_cny_userInfo)) {
            userInfo = new UserInfo(hand(array.getBigDecimal("btc")), array.getBigDecimal("cny"), array.getBigDecimal("ltc"));
        } else if (s.equals(spot_cny_orderInfo)) {//// TODO: 16-10-14 具体看数据结构
            orderInfo = new OrderInfo(hand(array.getBigDecimal("amount")), array.getBigDecimal("avg_price"), hand(array.getBigDecimal("deal_amount")), array.getString("result"));
        } else {
            System.out.println("without this operation :[" + s + "]");
        }
    }

    private static BigDecimal hand(BigDecimal value) {
        return value.setScale(BigDecimal.ROUND_DOWN, 2);
    }

    public static void main(String[] args) {
        String result = "[{ \"channel\":\"ok_sub_spotcny_btc_ticker\",\"data\":{\"buy\":4319.1,\"high\":4327.27,\"last\":\"4319.10\",\"low\":4297.3,\"sell\":4319.2,\"timestamp\":\"1476424388898\",\"vol\":\"1,191,131.62\"}}]";
        String tradeM = "[{\n" +
                "    \"channel\":\"ok_spotcny_trade\",\n" +
                "    \"data\":{\n" +
                "        \"order_id\":\"125433029\",\n" +
                "        \"result\":\"true\"\n" +
                "    }\n" +
                "}]";
        JSONObject object = JSON.parseArray(tradeM).getJSONObject(0);
//        JSONArray jsonArray = array.getJSONArray();
        System.out.println(object.get("channel"));
    }

    public synchronized UserInfo getUserInfoRet(String apiKey, String secretKey) {
        userInfo = null;
        client.getUserInfo(apiKey, secretKey);
        for (int i = 0; i < 3; i++) {
            sleep(333);
            if (userInfo != null)
                return userInfo;
        }
        return null;
    }

    public synchronized Trade getTradeRet(String apiKey, String secretKey, String symbol,
                             String price, String amount, String type) {
        trade = null;
        client.spotTrade(apiKey, secretKey, symbol, price, amount, type);
        for (int i = 0; i < 3; i++) {
            sleep(333);
            if (trade != null)
                return trade;
        }
        return null;
    }

    public synchronized CancelOrder getCancelOrderRet(String apiKey, String secretKey, String symbol,
                                         Long orderId) {
        cancelOrder = null;
        client.cancelOrder(apiKey, secretKey, symbol, orderId);
        for (int i = 0; i < 3; i++) {
            sleep(333);
            if (cancelOrder != null)
                return cancelOrder;
        }
        return null;
    }

    public synchronized OrderInfo getOrderInfoRet(String apiKey, String secretKey, String symbol, String orderId) {
        orderInfo = null;
        client.orderInfo(apiKey, secretKey, symbol, orderId);
        for (int i = 0; i < 3; i++) {
            sleep(333);
            if (orderInfo != null)
                return orderInfo;
        }
        return null;
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class UserInfo {//free

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
    }

    static class Trade {
        Trade(long orderId, String result) {
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
    }

    static class OrderInfo {
        OrderInfo(BigDecimal amount, BigDecimal avgPrice, BigDecimal dealAmount, String result) {
            this.amount = amount;
            this.avgPrice = avgPrice;
            this.dealAmount = dealAmount;
            this.result = result;
        }

        BigDecimal amount;
        BigDecimal avgPrice;
        BigDecimal dealAmount;
        String result;

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
    }

    static class CancelOrder {
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
    }
}

package huobi.impl;


import arbitrage.URL;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import huobi.IHuobiRestApi;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by tonyqi on 16-7-7.
 */
public class HuobiRestApi implements IHuobiRestApi {

    //请求参数方法的键
    private static final String method_k = "method";
    //请求参数创建时间的键
    private static final String created_k = "created";
    //请求参数apiKey的键
    private static final String apiKey_k = "access_key";
    //请求参数secretKey的键
    private static final String secretKey_k = "secret_key";
    //请求参数头寸的键
    private static final String currency_k = "coin_type";
    //请求参数价格的键
    private static final String price_k = "price";
    //请求参数头寸数量的键
    private static final String amount_k = "amount";
    //请求参数签名的键
    private static final String sign_k = "sign";
    //请求参数订单编号的键
    private static final String id_k = "id";


    @Override
    public BigDecimal ticker() {
        String huobiTickerUrl = "http://api.huobi.com/staticmarket/ticker_btc_json.js";
        String ret = URL.sendGet(huobiTickerUrl);
        if (ret == null)
            return null;
        JSONObject object = JSON.parseObject(ret).getJSONObject("ticker");
        return object.getBigDecimal("last");
    }

    public String trade(String apiKey, String secretKey, String currency, String price, String amount, String tradeDirection) {
        Map<String, String> paraMap = new TreeMap<>();
        if ("btc".equals(currency))
            currency = "1";//btc
        else
            currency = "2";//ltc
        paraMap.put(method_k, tradeDirection.toLowerCase());
        paraMap.put(created_k, EncryptUtil.getTimestamp());
        paraMap.put(apiKey_k, apiKey);
        paraMap.put(secretKey_k, secretKey);
        paraMap.put(currency_k, currency);
        paraMap.put(price_k, price);
        paraMap.put(amount_k, amount);
        String sign = EncryptUtil.getSign(paraMap);
        paraMap.remove(secretKey_k);
        paraMap.put(sign_k, sign);
        return URL.sendPost("", paraMap);
    }

    public String cancelOrder(String apiKey, String secretKey, String currency, String tradeId) {
        Map<String, String> paraMap = new TreeMap<>();
        paraMap.put(method_k, "cancel_order");
        paraMap.put(created_k, EncryptUtil.getTimestamp());
        paraMap.put(apiKey, apiKey);
        paraMap.put(secretKey_k, secretKey);
        if ("btc".equals(currency))
            currency = "1";//btc
        else
            currency = "2";//ltc
        paraMap.put(currency_k, currency);
        paraMap.put(id_k, tradeId);
        String sign = EncryptUtil.getSign(paraMap);
        paraMap.remove(secretKey_k);
        paraMap.put(sign_k, sign);
        return URL.sendPost("", paraMap);
    }

    public String userInfo(String apiKey, String secretKey) {
        Map<String, String> paraMap = new TreeMap<>();
        paraMap.put(method_k, "get_account_info");
        paraMap.put(created_k, EncryptUtil.getTimestamp());
        paraMap.put(apiKey_k, apiKey);
        paraMap.put(secretKey_k, secretKey);
        String sign = EncryptUtil.getSign(paraMap);
        paraMap.remove(secretKey_k);
        paraMap.put(sign_k, sign);
        return URL.sendPost("", paraMap);
    }

    public String orderInfo(String apiKey, String secretKey, String currency, String tradeId) {
        Map<String, String> paraMap = new TreeMap<>();
        paraMap.put(method_k, "order_info");
        paraMap.put(created_k, EncryptUtil.getTimestamp());
        paraMap.put(apiKey_k, apiKey);
        paraMap.put(secretKey_k, secretKey);
        if ("btc".equals(currency))
            currency = "1";//btc
        else
            currency = "2";//ltc
        paraMap.put(currency_k, currency);
        paraMap.put(id_k, tradeId);
        String sign = EncryptUtil.getSign(paraMap);
        paraMap.remove(secretKey_k);
        paraMap.put(sign_k, sign);
        return URL.sendPost("", paraMap);
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            sb.append(key).append("=").append(map.get(key)).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb.toString());
    }
}

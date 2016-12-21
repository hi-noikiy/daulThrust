package huobi;

import java.math.BigDecimal;

/**
 * Created by tonyqi on 16-7-7.
 */
public interface IHuobiRestApi {
    BigDecimal ticker() throws InterruptedException;

    /**
     * 下单交易
     *
     * @param apiKey
     * @param secretKey
     * @param currency       头寸，对应枚举 Currency （btc,ltc）底层单纯请求，未做参数验证等判断
     * @param price          价格
     * @param amount         数量
     * @param tradeDirection 交易方向－对应枚举TradeDirection
     * @return
     */
    String trade(String apiKey, String secretKey, String currency, String price, String amount, String tradeDirection);

    /**
     * 取消挂单
     *
     * @param apiKey
     * @param secretKey
     * @param currency  头寸，对应枚举 Currency （btc,ltc）底层单纯请求，未做参数验证等判断
     * @param tradeId   订单编号
     * @return
     */
    String cancelOrder(String apiKey, String secretKey, String currency, String tradeId);

    /**
     * 账户信息
     *
     * @param apiKey
     * @param secretKey
     * @return
     */
    String userInfo(String apiKey, String secretKey);

    /**
     * 挂单详情
     *
     * @param apiKey
     * @param secretKey
     * @param currency  　头寸，对应枚举 Currency （btc,ltc）底层单纯请求，未做参数验证等判断
     * @param tradeId
     * @return
     */
    String orderInfo(String apiKey, String secretKey, String currency, String tradeId);
}

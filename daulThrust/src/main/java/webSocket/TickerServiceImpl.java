package webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.KLineList;

/**
 * Created by tonyqi on 16-10-13.
 */
public class TickerServiceImpl implements WebSocketService {
    public void onReceive(String msg) {
        if (msg.contains("data")) {
            KLineList.getInstance().setLastPrice(JSON.parseArray(msg).getJSONObject(0).getJSONObject("data").getBigDecimal("last"));
        }
    }

    public static void main(String[] args) {
        String msg = "[{\"channel\":\"ok_sub_spotcny_btc_ticker\",\"data\":{\"buy\":\"4316.39\",\"high\":\"4327.27\",\"last\":\"4316.56\",\"low\":\"4297.3\",\"sell\":\"4316.4\",\"timestamp\":\"1476434179658\",\"vol\":\"1,123,675.10\"}}]";
        JSONObject array = JSON.parseArray(msg).getJSONObject(0).getJSONObject("data");
        System.out.println();
    }
}

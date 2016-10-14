package webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import model.KLineList;
import model.Kline;
import org.apache.log4j.Logger;
import strategy.DaulThrust;

public class KlineServiceImpl implements WebSocketService {
    private Logger log = Logger.getLogger(WebSocketBase.class);
    private KLineList kLineList = KLineList.getInstance();
    private static DaulThrust daulThrust = new DaulThrust();


    public void onReceive(String msg) {
//        System.out.println(msg);

        if (msg.contains("data")) {
            JSONArray array = JSON.parseArray(msg).getJSONObject(0).getJSONArray("data");
            if (array.size() == 1) {
                for (int i = 0; i < array.size(); i++) {
                    JSONArray a = array.getJSONArray(i);
                    Kline kline = new Kline(a.getLong(0), a.getBigDecimal(1), a.getBigDecimal(2), a.getBigDecimal(3), a.getBigDecimal(4), a.getBigDecimal(5));
                    if (kLineList.setKline(kline)) {
                        daulThrust.order();
                    }
                }
            }
        }
//        System.out.println(msg);
    }
}

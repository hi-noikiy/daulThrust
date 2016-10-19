package webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import model.KLineList;
import model.Kline;
import model.SimpleKline;
import strategy.DaulThrust;

public class KlineServiceImpl implements WebSocketService {
    private KLineList kLineList = KLineList.getInstance();
    private static DaulThrust daulThrust = new DaulThrust();
    private static SimpleKline simpleKline = new SimpleKline();


    public void onReceive(String msg) {

        if (msg.contains("data")) {
            JSONArray array = JSON.parseArray(msg).getJSONObject(0).getJSONArray("data");
//            if (array.size() == 2) {
//                for (int i = 0; i < array.size(); i++) {
//                    JSONArray a = array.getJSONArray(i);
//                    Kline kline = new Kline(a.getLong(0), a.getBigDecimal(1), a.getBigDecimal(2), a.getBigDecimal(3), a.getBigDecimal(4), a.getBigDecimal(5));
////                    if (kLineList.setKline(kline)) {
////                        daulThrust.order();
////                    }
//                    setKline(kline);
//
//                }
//            } else
            if (array.size() == 6) {
                Kline kline = new Kline(array.getLong(0), array.getBigDecimal(1), array.getBigDecimal(2), array.getBigDecimal(3), array.getBigDecimal(4), array.getBigDecimal(5));
//                if (kLineList.setKline(kline)) {
//                    daulThrust.order();
//                }
                setKline(kline);
            }
        }
    }

    private synchronized void setKline(Kline kline) {
        simpleKline.setKline(kline);
    }
}

package webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import model.KLineList;
import model.Kline;
import model.SimpleKline;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import strategy.DaulThrust;

public class KlineServiceImpl implements WebSocketService {
    private KLineList kLineList = KLineList.getInstance();
    private static DaulThrust daulThrust = new DaulThrust();
    private static SimpleKline simpleKline = new SimpleKline();
    Log log = LogFactory.getLog(SimpleKline.class);

    public synchronized void onReceive(String msg) {
        if (msg.contains("data")) {
            JSONArray array = JSON.parseArray(msg).getJSONObject(0).getJSONArray("data");
            if (array.size() == 2) {
                for (int i = 0; i < array.size(); i++) {
                    JSONArray jsonArray = array.getJSONArray(i);
                    Kline kline;
                    try {
                        kline = new Kline(jsonArray.getLong(0), jsonArray.getBigDecimal(1), jsonArray.getBigDecimal(2), jsonArray.getBigDecimal(3), jsonArray.getBigDecimal(4), jsonArray.getBigDecimal(5));
                    } catch (RuntimeException ex) {
                        log.error("in size = 2 okcoin's data is Exception :" + ex.getMessage());
                        return;
                    }
//                    if (kLineList.setKline(kline)) {
//                        daulThrust.order();
//                    }
//                    setKline(kline);
                    System.out.println(kline);
                }
            } else if (array.size() == 6) {
                Kline kline;
                try {
                    kline = new Kline(array.getLong(0), array.getBigDecimal(1), array.getBigDecimal(2), array.getBigDecimal(3), array.getBigDecimal(4), array.getBigDecimal(5));
                } catch (RuntimeException ex) {
                    log.error("in size = 6 okcoin's data is Exception :" + ex.getMessage());
                    return;
                }
//                setKline(kline);
                System.out.println(kline);
            }
        }
    }

    private synchronized void setKline(Kline kline) {
        simpleKline.setKline(kline);
    }
}

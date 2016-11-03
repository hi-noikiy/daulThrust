package webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import model.Kline;
import model.SimpleKline;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import strategy.ApiResult;

import java.util.concurrent.LinkedBlockingQueue;

class KlineServiceImpl implements WebSocketService {
    private static SimpleKline simpleKline = new SimpleKline();
    private Log log = LogFactory.getLog(KlineServiceImpl.class);

    private int count = 0;
    public boolean flag = true;
    private static String apiKey = "8a134f3f-51b8-4993-a916-bad635fdaf15";
    private static String secretKey = "D67E022D0242FC3FE737C78BF8B546E4";
    private static LinkedBlockingQueue<JSONObject> msgQueue = new LinkedBlockingQueue<JSONObject>(10);//满2条阻塞

    public void onReceive(String message) {
        if (message.contains("data")) {
            JSONObject object = JSON.parseArray(message).getJSONObject(0);
            String channel = object.getString("channel");
            if (channel.equals(ApiResult.spot_cny_trade)
                    || channel.equals(ApiResult.spot_cny_cancel)
                    || channel.equals(ApiResult.spot_cny_orderInfo)
                    || channel.equals(ApiResult.spot_cny_userInfo)) {
                ApiResult.setMsg(message);//主动请求返回的数据
                return;
            }
            try {
                msgQueue.put(object);
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }

            if (count == 0) {//只执一次
                new ThreadX().start();
                count++;
            }
        } else if (message.contains("channel") && message.contains("errorcode")) {
            log.error(message);
        }
    }

    private synchronized void setKline(Kline kline) {
        simpleKline.setKline(kline);
    }

    private class ThreadX extends Thread {
        @Override
        public void run() {
            while (flag) {
                JSONObject object = null;
                try {
                    object = msgQueue.take();//为空则阻塞
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
                if (object != null) {
                    JSONArray array = object.getJSONArray("data");
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
                            setKline(kline);
                        }
                    } else if (array.size() == 6) {
                        Kline kline;
                        try {
                            kline = new Kline(array.getLong(0), array.getBigDecimal(1), array.getBigDecimal(2), array.getBigDecimal(3), array.getBigDecimal(4), array.getBigDecimal(5));
                        } catch (RuntimeException ex) {
                            log.error("in size = 6 okcoin's data is Exception :" + ex.getMessage());
                            return;
                        }
                        setKline(kline);
                    }
                }
            }
            System.exit(0);
        }
    }
}

package webSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;

public class Example {

    public static WebSoketClient client;
    public static int minStrategy = 60;

    public static void main(String[] args) {
        String msg = "参数错误,启动程序需要一个必要参数，可选1/5/15/30/60";
        try {
            minStrategy = Integer.valueOf(args[0]);
        } catch (RuntimeException ex) {
            System.out.println(msg);
            System.exit(0);
        }
        if (minStrategy != 1 && minStrategy != 5 && minStrategy != 15 && minStrategy != 30 && minStrategy != 60) {
            System.out.println(msg);
            System.exit(0);
        }
        String arg = "min";
        loadProperties(minStrategy + arg);

        Log log = LogFactory.getLog(Example.class);

        log.info("start " + minStrategy + " " + arg + " strategy");

        // apiKey 为用户申请的apiKey
        String apiKey = "XXXXX";

        // secretKey为用户申请的secretKey
        String secretKey = "XXXXX";

        apiKey = "dfbb23da-5cac-4bec-b4ce-ed828f8a5458";
        secretKey = "B0FA4F10E14364A0C7562DDFBBFD432C";

        // 国际站WebSocket地址 注意如果访问国内站 请将 real.okcoin.com 改为 real.okcoin.cn
        String url = "wss://real.okcoin.cn:10440/websocket/okcoinapi";

        // 订阅消息处理类,用于处理WebSocket服务返回的消息
        WebSocketService service = new KlineServiceImpl();

        // WebSocket客户端
        client = new WebSoketClient(url, service);

        // 启动客户端
        client.start();

        // 添加订阅
        client.addChannel("ok_sub_spotcny_btc_kline_" + minStrategy + arg);

        WebSocketService serviceTicker = new TickerServiceImpl();
        WebSoketClient clientTicker = new WebSoketClient(url, serviceTicker);
        clientTicker.start();
        clientTicker.addChannel("ok_sub_spotcny_btc_ticker");

        // 删除定订阅
        // client.removeChannel("ok_sub_spotusd_btc_ticker");

        // 合约下单交易
        // client.futureTrade(apiKey, secretKey, "btc_usd", "this_week", 2.3, 2,
        // 1, 0, 10);

        // 实时交易数据 apiKey
//         client.futureRealtrades(apiKey, secretKey);

        // 取消合约交易
        // client.cancelFutureOrder(apiKey, secretKey, "btc_usd", 123456L,
        // "this_week");

        // 现货下单交易
//         client.spotTrade(apiKey, secretKey, "btc_usd", "400", "0.1", "sell");

        // 现货交易数据
        // client.realTrades(apiKey, secretKey);

        // 现货取消订单
        // client.cancelOrder(apiKey, secretKey, "btc_usd", 123L);

        // 获取用户信息
//         client.getUserInfo(apiKey,secretKey);
    }

    private static void loadProperties(String msg) {
        Properties pps = new Properties();
        try {
            pps.load(Example.class.getClassLoader().getResourceAsStream("log4j.properties"));
        } catch (IOException e) {
            System.out.println("配置文件读取失败,运行结束");
            System.exit(0);
        }
        String path = "/home/tonyqi/log/strategy" + msg;

        pps.setProperty("log4j.appender.file.File", path);

        PropertyConfigurator.configure(pps);
    }

//    public static void main(String[] args) {
//        PropertyConfigurator.configure(Example.class.getClassLoader().getResourceAsStream("log4j.properties"));
//    }
}
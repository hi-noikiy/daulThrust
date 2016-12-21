package huobi.impl;

import java.security.MessageDigest;
import java.util.Map;

/**
 * Created by tonyqi on 16-7-7.
 */
class EncryptUtil {
    public static String MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] btInput = s.getBytes("utf-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getSign(Map<String, String> map) {
        if (map == null || map.size() < 1)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet())
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        return MD5(stringBuilder.substring(0, stringBuilder.length() - 1));
    }

    static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }
}

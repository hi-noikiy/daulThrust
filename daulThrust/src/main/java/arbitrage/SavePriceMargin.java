package arbitrage;


import java.math.BigDecimal;

/**
 * Created by tonyqi on 16-12-20.
 */
public class SavePriceMargin {
    public static Long lastTime = System.currentTimeMillis();

    //比较价格,1.5s比较一次
    public static void startMargin(BigDecimal okcoinPrice) {
        Long now;
        if ((now = System.currentTimeMillis()) - lastTime < 1500)
            return;
        lastTime = null;

    }
}

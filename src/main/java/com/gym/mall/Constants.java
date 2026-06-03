package com.gym.mall;

public class Constants {

    public static final String LIKE_USER_KEY = "LIKE_USER_KEY:";

    public static final String LIKE_STATISTIC_KEY="LIKE_STATISTIC_KEY:";

    public static final String COMMODITY_KEY = "COMMODITY_KEY:";

    /** 购物车 Redis Key 前缀：cart:{userId} → Hash(commodityId -> quantity) */
    public static final String CART_KEY = "cart:";

    /** 订单 ID 缓存前缀 */
    public static final String ORDER_KEY = "order:";

    /** 库存锁定 Key 前缀 */
    public static final String STOCK_LOCK_KEY = "stock:lock:";

    /** 订单支付超时时间（分钟） */
    public static final long PAYMENT_TIMEOUT_MINUTES = 30;
}

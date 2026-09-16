package com.zifang.util.core.extra;

/**
 * 经纬度坐标计算工具类
 * <p>
 * 提供地球上两点之间距离的计算功能，基于Haversine公式计算球面距离
 *
 * @author zifang
 */
public final class CoordinateUtil {
    /**
     * 计算地球上任意两点之间的球面距离
     * <p>
     * 使用 Haversine 公式计算两个经纬度坐标点之间的最短距离（地球表面大圆距离）
     *
     * @param long1 第一点经度，单位：度，取值范围 [-180, 180]
     * @param lat1  第一点纬度，单位：度，取值范围 [-90, 90]
     * @param long2 第二点经度，单位：度，取值范围 [-180, 180]
     * @param lat2  第二点纬度，单位：度，取值范围 [-90, 90]
     * @return 两点之间的距离，单位：米
     * @throws IllegalArgumentException 如果经纬度参数超出合法范围
     */
    public static double distance(double long1, double lat1, double long2, double lat2) {
        double a, b, R;
        R = 6378137; // 地球半径
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (long1 - long2) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2
                * R
                * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
                * Math.cos(lat2) * sb2 * sb2));
        return d;
    }
}

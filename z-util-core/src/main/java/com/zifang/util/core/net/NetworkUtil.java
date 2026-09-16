package com.zifang.util.core.net;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.zifang.util.core.Const.Symbol.MINUS;


/**
 * 网络操作类
 */
public class NetworkUtil {

    /**
     * 默认的localhost
     */
    public static final String DEFAULT_LOCALHOST = "127.0.0.1";

    /**
     * 获取本机MAC地址
     *
     * @param macConnector MAC地址各段之间的连接符，如 "-" 或 ":"
     * @return MAC地址字符串
     * @throws SocketException      如果发生网络接口访问错误
     * @throws UnknownHostException 如果无法确定本地主机地址
     */
    public static String getMac(String macConnector) throws SocketException, UnknownHostException {
        StringBuilder macAddress = new StringBuilder();
        List<String> ipList = getLocalHostAddress();
        for (String str : ipList) {
            // 获取本地IP对象
            InetAddress address = InetAddress.getByName(str);
            // 获得网络接口对象(即网卡), 并得到mac地址, mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(address).getHardwareAddress();
            if (null != mac) {
                // 下面代码是把mac地址拼装成String
                StringBuilder segment = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    if (i != 0) {
                        segment.append(macConnector);
                    }
                    // mac[i] & 0xFF 是为了把byte转化为正整数
                    String hexMac = Integer.toHexString(mac[i] & 0xFF);
                    segment.append(hexMac.length() == 1 ? 0 + hexMac : hexMac);
                }
                // 把字符串所有小写字母改为大写成为正规的mac地址并返回
                macAddress.append(segment.toString().toUpperCase());
            }
        }
        return macAddress.toString();
    }

    /**
     * 获取本机MAC地址，使用默认连接符（短横线"-"）连接各段
     *
     * @return MAC地址字符串，格式如 "AA-BB-CC-DD-EE-FF"
     * @throws SocketException      如果发生网络接口访问错误
     * @throws UnknownHostException 如果无法确定本地主机地址
     */
    public static String getMac() throws SocketException, UnknownHostException {
        return getMac(MINUS);
    }

    /**
     * 获取本机IP地址列表（仅IPv4，排除回环地址127.0.0.1）
     *
     * @return 本机所有有效IPv4地址列表
     * @throws SocketException 如果发生网络接口访问错误
     */
    public static List<String> getLocalHostAddress() throws SocketException {
        List<String> ipList = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address) {
                    if (!DEFAULT_LOCALHOST.equals(address.getHostAddress())) {
                        ipList.add(address.getHostAddress());
                    }
                }
            }
        }
        return ipList;
    }


    /**
     * 获取本机第一个有效IPv4地址
     *
     * @return 本机第一个有效IPv4地址，如果获取失败则返回默认回环地址 "127.0.0.1"
     */
    public static String getFirstLocalHostAddress() {
        List<String> ipList;
        try {
            ipList = getLocalHostAddress();
            // 取最后一个
            return ipList.get(ipList.size() - 1);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return DEFAULT_LOCALHOST;
    }

    /**
     * 将IPv4地址字符串转换为整数
     *
     * @param ipStr IPv4地址字符串，格式如 "192.168.1.1"
     * @return IPv4地址对应的32位整数
     */
    public static int ipToInt(String ipStr) {
        String[] ip = ipStr.split("\\.");
        return (Integer.parseInt(ip[0]) << 24) + (Integer.parseInt(ip[1]) << 16) + (
                Integer.parseInt(ip[2]) << 8) + Integer.parseInt(ip[3]);
    }

    /**
     * 将32位整数转换为IPv4地址字符串
     *
     * @param intIp 32位整数表示的IPv4地址
     * @return IPv4地址字符串，格式如 "192.168.1.1"
     */
    public static String intToIp(int intIp) {
        return (intIp >>> 24) + "."
                + ((intIp >>> 16) & 0xFF) + "."
                + ((intIp >>> 8) & 0xFF) + "."
                + (intIp & 0xFF);
    }

    /**
     * 获取本地第一个有效IPv4地址（非回环、非禁用、非IPv6）
     *
     * @return 本地IP字符串，获取失败返回null
     */
    public static String getLocalIp() {
        try {
            // 遍历所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                // 过滤：未启用的接口、回环接口、虚拟接口（如Docker桥接）
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                // 遍历接口绑定的所有地址
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();
                    // 过滤：IPv6地址、回环地址（127.0.0.1）
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        // 若未找到有效IP，返回回环地址作为 fallback
        return InetAddress.getLoopbackAddress().getHostAddress();
    }

    /**
     * 获取所有本地有效IPv4地址
     *
     * @return 有效IPv4地址数组
     * @throws UnknownHostException 如果无法确定本地主机名
     */
    public static String[] getAllLocalIps() throws UnknownHostException {
        return java.util.stream.Stream.of(InetAddress.getAllByName(getLocalHostName()))
                .filter(addr -> addr instanceof Inet4Address && !addr.isLoopbackAddress())
                .map(InetAddress::getHostAddress)
                .toArray(String[]::new);
    }

    /**
     * 获取本地主机名
     */
    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * main方法。
     * @param args String[]类型参数
     *
     */
    public static void main(String[] args) throws UnknownHostException {
        System.out.println("自定义NetUtil获取本地IP：" + NetworkUtil.getLocalIp());
        System.out.println("所有本地有效IPv4：");
        for (String ip : NetworkUtil.getAllLocalIps()) {
            System.out.println("- " + ip);
        }
    }

}

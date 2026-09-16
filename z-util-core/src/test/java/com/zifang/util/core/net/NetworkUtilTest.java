package com.zifang.util.core.net;

import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * NetworkUtilTest类。
 */
public class NetworkUtilTest {

    // --- getMac ---

    @Test
    /**
     * testGetMac_WithDefaultConnector方法。
     */
    public void testGetMac_WithDefaultConnector() throws Exception {
        String mac = NetworkUtil.getMac();
        assertNotNull(mac);
        assertFalse(mac.isEmpty());
        // MAC address should contain hyphens
        assertTrue(mac.contains("-"));
    }

    @Test
    /**
     * testGetMac_WithColonConnector方法。
     */
    public void testGetMac_WithColonConnector() throws Exception {
        String mac = NetworkUtil.getMac(":");
        assertNotNull(mac);
        assertFalse(mac.isEmpty());
        assertTrue(mac.contains(":"));
    }

    @Test
    /**
     * testGetMac_UpperCase方法。
     */
    public void testGetMac_UpperCase() throws Exception {
        String mac = NetworkUtil.getMac();
        assertEquals(mac.toUpperCase(), mac);
    }

    // --- getLocalHostAddress ---

    @Test
    /**
     * testGetLocalHostAddress方法。
     */
    public void testGetLocalHostAddress() throws Exception {
        List<String> addresses = NetworkUtil.getLocalHostAddress();
        assertNotNull(addresses);
    }

    // --- getFirstLocalHostAddress ---

    @Test
    /**
     * testGetFirstLocalHostAddress方法。
     */
    public void testGetFirstLocalHostAddress() {
        String address = NetworkUtil.getFirstLocalHostAddress();
        assertNotNull(address);
        assertFalse(address.isEmpty());
    }

    // --- ipToInt ---

    @Test
    /**
     * testIpToInt_WithLocalhost方法。
     */
    public void testIpToInt_WithLocalhost() {
        int result = NetworkUtil.ipToInt("127.0.0.1");
        assertEquals(2130706433, result);
    }

    @Test
    /**
     * testIpToInt_WithAllZeros方法。
     */
    public void testIpToInt_WithAllZeros() {
        int result = NetworkUtil.ipToInt("0.0.0.0");
        assertEquals(0, result);
    }

    // Note: For IPs > 127.255.255.255, int overflow occurs (e.g. 192.168.1.1 -> -1062731519)
    // This is expected behavior of the int return type

    // --- intToIp ---

    @Test
    /**
     * testIntToIp_WithZero方法。
     */
    public void testIntToIp_WithZero() {
        String result = NetworkUtil.intToIp(0);
        assertEquals("0.0.0.0", result);
    }

    @Test
    /**
     * testIntToIp_WithMinusOne方法。
     */
    public void testIntToIp_WithMinusOne() {
        String result = NetworkUtil.intToIp(-1);
        // -1 as signed int is 255.255.255.255 as unsigned
        assertEquals("255.255.255.255", result);
    }

    @Test
    /**
     * testIntToIp_WithLocalhostInt方法。
     */
    public void testIntToIp_WithLocalhostInt() {
        // 127.0.0.1 = 2130706433 (fits in int)
        String result = NetworkUtil.intToIp(2130706433);
        assertEquals("127.0.0.1", result);
    }

    // --- getLocalIp ---

    @Test
    /**
     * testGetLocalIp方法。
     */
    public void testGetLocalIp() {
        String ip = NetworkUtil.getLocalIp();
        assertNotNull(ip);
        assertFalse(ip.isEmpty());
        // Should be IPv4 format
        assertTrue(ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || ip.equals("localhost"));
    }

    // --- getAllLocalIps ---

    @Test
    /**
     * testGetAllLocalIps方法。
     */
    public void testGetAllLocalIps() throws UnknownHostException {
        String[] ips = NetworkUtil.getAllLocalIps();
        assertNotNull(ips);
    }

    // --- DEFAULT_LOCALHOST ---

    @Test
    /**
     * testDefaultLocalhost方法。
     */
    public void testDefaultLocalhost() {
        assertEquals("127.0.0.1", NetworkUtil.DEFAULT_LOCALHOST);
    }
}
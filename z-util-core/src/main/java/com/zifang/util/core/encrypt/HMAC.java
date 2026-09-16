package com.zifang.util.core.encrypt;


import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

/**
 * 序
 * <p>
 * 背景
 * <p>
 * HMAC是单向加密算法中的一种，而且还是基于密钥的哈希算法的认证协议。
 * <p>
 * 正文
 * <p>
 * HMAC，全称为“Hash Message Authentication Code”，中文名“散列消息鉴别码”，主要是利用哈希算法，以一个密钥和一个消息为输入，
 * 生成一个消息摘要作为输出。一般的，消息鉴别码用于验证传输于两个共 同享有一个密钥的单位之间的消息。HMAC 可以与任何迭代散列函数捆绑使用。
 * MD5 和 SHA-1 就是这种散列函数。HMAC 还可以使用一个用于计算和确认消息鉴别值的密钥。
 * <p>
 * HMAC，散列消息鉴别码，是基于密钥的 Hash 算法的认证协议。它的实现原理是，用公开函数和密钥产生一个固定长度的值作为认证标识，用这个标识鉴别消息的完整性。
 * 使用一个密钥生成一个固定大小的小数据块，即 MAC，并将其加入到消息中，然后传输。接收方利用与发送方共享的密钥进行鉴别认证等。
 * <p>
 * 这种结构的主要作用是：
 * 不用修改就可以使用适合的散列函数，而且散列函数在软件方面表现的很好， 并且源码是公开和通用的。
 * 可以保持散列函数原有的性能而不致使其退化。
 * 可以使得基于合理的关于底层散列函数假设的消息鉴别机制的加密强度分析 便于理解。
 * 当发现或需要运算速度更快或更安全的散列函数时，可以很容易的实现底层 散列函数的替换。
 * <p>
 * 定义 HMAC 需要一个加密用散列函数（表示为 H）和一个密钥 K。我们假设 H 是 一个将数据块用一个基本的迭代压缩函数来加密的散列函数。
 * 我们用 B 来表示数据块的字长。（以上提到的散列函数的分割数据块字长 B = 64），用 L 来表示散列函数的输出数据字长（MD5中 L = 16 , SHA-1 中 L = 20）。
 * 鉴别密钥的长度可以是小于等于数据块字长的任何正整数值。应用程序中使用的密钥长度若是比 B 大，则首先用使用散列函数 H 作用于它，
 * 然后用 H 输出的 L 长度字符串作为在 HMAC 中实际使用的密钥。一般情况下，推荐的最小密钥 K 长度是 L 个字长。（与 H 的输出数据长度相等）。
 * <p>
 * 我们将定义两个固定且不同的字符串 ipad，opad：（‘i’，‘o’表示内部与外部）
 * <p>
 * <p>
 * ipad = the byte 0x36 repeated B times
 * opad = the byte 0x5C repeated B times
 * <p>
 * 计算‘text’的 HMAC：
 * <p>
 * H (K XOR opad, H (K XOR ipad, text))
 * <p>
 * 计算步骤
 * <p>
 * 在密钥 K 后面添加 0 来创建一个子长为 B 的字符串。(例如，如果 K 的字长是 20 字节，B＝60 字节，则 K 后会加入 44 个零字节0x00)
 * 将上一步生成的 B 字长的字符串与 ipad 做异或运算
 * 将数据流 text 填充至第二步的结果字符串中
 * 用 H 作用于第三步生成的数据流
 * 将第一步生成的 B 字长字符串与 opad 做异或运算
 * 再将第四步的结果填充进第五步的结果中
 * 用 H 作用于第六步生成的数据流，输出最终结果
 * <p>
 * 密钥
 * <p>
 * 用于 HMAC 的密钥可以是任意长度（比 B 长的密钥将首先被 H 处理）。但当密钥 长度小于 L 时，会降低函数的安全强度。长度大于 L 的密钥也是可以的，
 * 但额外的长度并不能显著的提高函数的安全强度。
 * <p>
 * 密钥必须随机选取(或使用强大的基于随机种子的伪随机生成方法)，并且要周期性的更新。目前的攻击没有指出一个有效的更换密钥的频率，因为那些攻击实际上并不可行。
 * 然而，周期性更新密钥是一个对付函数和密钥所存在的潜在缺陷的基本的安全措施，并可以降低泄漏密钥带来的危害。
 *
 * @author zifang
 * @version 1.0
 * @Company: Beijing INFCN Software Co.,Ltd.
 * @ClassName: HMAC
 * @Project: jdk_demo
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2019年12月1日 下午4:49:20
 */
public class HMAC {
    /**
     * 定义加密方式
     * MAC算法可选以下多种算法
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    private final static String KEY_MAC = "HmacMD5";

    /**
     * 全局数组
     */
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 构造函数
     */
    public HMAC() {

    }

    /**
     * BASE64 加密
     *
     * @param key 需要加密的字节数组
     * @return 字符串
     * @throws Exception
     */
    public static String encryptBase64(byte[] key) throws Exception {
        return java.util.Base64.getEncoder().encodeToString(key);
    }

    /**
     * BASE64 解密
     *
     * @param key 需要解密的字符串
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] decryptBase64(String key) throws Exception {
        return java.util.Base64.getDecoder().decode((key));
    }

    /**
     * 初始化HMAC密钥
     *
     * @return
     */
    public static String init() {
        SecretKey key;
        String str = "";
        try {
            KeyGenerator generator = KeyGenerator.getInstance(KEY_MAC);
            key = generator.generateKey();
            str = encryptBase64(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * HMAC加密
     *
     * @param data 需要加密的字节数组
     * @param key  密钥
     * @return 字节数组
     */
    public static byte[] encryptHMAC(byte[] data, String key) {
        SecretKey secretKey;
        byte[] bytes = null;
        try {
            secretKey = new SecretKeySpec(decryptBase64(key), KEY_MAC);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * HMAC加密
     *
     * @param data 需要加密的字符串
     * @param key  密钥
     * @return 字符串
     */
    public static String encryptHMAC(String data, String key) {
        if (data == null || data.trim().equals("")) {
            return null;
        }
        byte[] bytes = encryptHMAC(data.getBytes(), key);
        return byteArrayToHexString(bytes);
    }


    /**
     * 将一个字节转化成十六进制形式的字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    private static String byteToHexString(byte b) {
        int ret = b;
        //System.out.println("ret = " + ret);  
        if (ret < 0) {
            ret += 256;
        }
        int m = ret / 16;
        int n = ret % 16;
        return hexDigits[m] + hexDigits[n];
    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToHexString(bytes[i]));
        }
        return sb.toString();
    }

    /**
     * 测试方法
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String key = HMAC.init();
        System.out.println("Mac密钥:" + key);
        String word = "123";
        System.out.println(encryptHMAC(word, key));
    }
}
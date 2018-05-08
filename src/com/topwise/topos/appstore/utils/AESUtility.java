package com.topwise.topos.appstore.utils;

/*
 * Copyright (C) 2015 Baidu Inc. All rights reserved.
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

/**
 * 参考《百度数据加密算法》
 * http://m.baidu.com/api?bdi_docs=1&action=intro&source=intro_extrainfo2
 * &cur=intro
 *
 *
 获取AES加密需要的key。将渠道号与密钥（API平台提供）拼接，然后获取拼接生成字符串的md5值并取后16位，最后将截取后的结果全部转为大写。
 处理AES加密需要的iv值。iv向API平台申请即可，如果申请到的iv值不足16位，需要用字符“0”补足。
 填充原文数据。具体算法见示例。
 AES加密模式为CBC，实现算法为RIJNDAEL算法。具体初始化方式见示例。
 生成的密文需要进行base64编码处理，并进行urlencode。
 * @author yuanxingzhong
 * @since 2015年12月16日
 */
public final class AESUtility {

	public static String PKEY = "";

	private static String PKEY_ALPHAGO = "1000";
	private static String PKEY_17WO = "1001";
	private static String PKEY_IVVI = "1005";
	private static String PKEY_COOLMART = "1009";
	private static String PKEY_SHARP = "1011";
	private static String PKEY_DUOCAI = "1016";
	private static String PKEY_DINGZHI = "1017";

	public static Map<String, String> PSECRETS = new HashMap<String, String>();
	static {
		PSECRETS.put(PKEY_ALPHAGO, "bgt56yhn2wsxtyhnbg");
		PSECRETS.put(PKEY_17WO, "17wosdkwoju20171124");
		PSECRETS.put(PKEY_IVVI, "bgt56yhn2wsxtyhnbg");
		PSECRETS.put(PKEY_COOLMART, "coolmartcoolmartappstore");
		PSECRETS.put(PKEY_SHARP, "sharpsharpappstore");
		PSECRETS.put(PKEY_DUOCAI, "duocaiappstore20171226");
		PSECRETS.put(PKEY_DINGZHI, "dingzhiappstore20180130");
	}
	public static Map<String, String> IV = new HashMap<String, String>();
	static {
		IV.put(PKEY_ALPHAGO, "zaq12wsxcde34rfv");
		IV.put(PKEY_17WO, "zaq12wsxcde34rfv");
		IV.put(PKEY_IVVI, "zaq12wsxcde34rfv");
		IV.put(PKEY_COOLMART, "zaq12wsxcde34rfv");
		IV.put(PKEY_SHARP, "zaq12wsxcde34rfv");
        IV.put(PKEY_DUOCAI, "zaq12wsxcde34rfv");
        IV.put(PKEY_DINGZHI, "zaq12wsxcde34rfv");
	}

	/**
	 * 加密，先aes，再base64再urlencode
	 * 
	 * @param rawStr
	 * @param pkey
	 * @param iv
	 * @return
	 */
	public static String encode(String rawStr, String pkey, String psecret,
			String iv) {
		if (null == rawStr || null == psecret || rawStr.isEmpty()
				|| psecret.isEmpty()) {
			return null; // 加密的必要参数为空，加密失败！
		}
		byte[] aesBytes = AESEncode(rawStr, pkey, psecret, iv);
		if (null == aesBytes) {
			return null; // aes加密失败
		}

		String base64Encode = Base64.encodeToString(aesBytes, Base64.NO_WRAP);

		String urlEncodeStr = URLEncoder.encode(base64Encode);

		return urlEncodeStr;
	}

	/**
	 * 解密
	 * 
	 * @param rawStr
	 * @param pkey
	 * @param psecret
	 * @param iv
	 * @return
	 */
	public static String decode(String rawStr, String pkey, String psecret,
			String iv) {
		if (null == rawStr || null == psecret || rawStr.isEmpty()
				|| psecret.isEmpty()) {
			return null; // 解密的必要参数为空，加密失败！
		}
		byte[] str = Base64.decode(URLDecoder.decode(rawStr), Base64.DEFAULT);

		byte[] aesBytes = AESDecode(str, pkey, psecret, iv);
		if (null == aesBytes) {
			return null; // aes加密失败
		}
		return new String(aesBytes);
	}

	public static byte[] AESDecode(byte[] rawStr, String from, String key,
			String iv) {
		try {
			IvParameterSpec ivParam = null;
			if (null != iv && !iv.isEmpty()) {
				ivParam = new IvParameterSpec(generateIV(iv));
			}

			String strMd5 = toMd5(from + key);
			if (null == strMd5) {
				return null; // 生成key失败！
			}
			byte[] strBytes = strMd5.getBytes("utf-8");
			SecretKeySpec cipherKey = new SecretKeySpec(strBytes,
					strBytes.length / 2, strBytes.length / 2, "AES");
			Cipher decodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			if (null != ivParam) {
				decodeCipher.init(Cipher.DECRYPT_MODE, cipherKey, ivParam);
			} else {
				decodeCipher.init(Cipher.DECRYPT_MODE, cipherKey);
			}
			return decodeCipher.doFinal(rawStr);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 对字符串进行aes加密
	 * 
	 * @param rawStr
	 * @param key
	 * @param iv
	 * @return
	 */
	private static byte[] AESEncode(String rawStr, String from, String key,
			String iv) {
		try {
			IvParameterSpec ivParam = null;
			if (null != iv && !iv.isEmpty()) {
				ivParam = new IvParameterSpec(generateIV(iv));
			}

			String strMd5 = toMd5(from + key);
			if (null == strMd5) {
				return null; // 生成key失败！
			}
			byte[] strBytes = strMd5.getBytes("utf-8");

			SecretKeySpec cipherKey = new SecretKeySpec(strBytes,
					strBytes.length / 2, strBytes.length / 2, "AES");

			Cipher encodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			if (null != ivParam) {
				encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivParam);
			} else {
				encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey);
			}

			return encodeCipher.doFinal(rawStr.getBytes("utf-8"));

		} catch (Exception e) {
		}
		return null;
	}

	public static byte[] generateIV(String iv) {
		if (null == iv || iv.isEmpty()) {
			return null;
		}
		try {
			byte[] ivBytes = iv.getBytes("utf-8");
			return Arrays.copyOf(ivBytes, 16);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * 获取字符串的md5
	 * 
	 * @param str
	 * @return
	 */
	public static String toMd5(String str) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(str.getBytes("utf-8"));
			if (null != bytes) {
				return parseByte2HexStr(bytes);
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		if (null == buf) {
			return null;
		}
		try {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < buf.length; i++) {
				String hex = Integer.toHexString(buf[i] & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				sb.append(hex.toUpperCase());
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

}
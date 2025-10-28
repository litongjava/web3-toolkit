package com.litongjava.web3.utils;

import java.util.Arrays;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

public class Base58Utils {

  // Base58 编码使用的字符集
  public static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
  /**
   * 将字节数组编码为 Base58 字符串。
   * 
   * @param input 输入字节数组
   * @return Base58 编码字符串
   */
  public static String base58Encode(byte[] input) {
    if (input.length == 0) {
      return "";
    }

    // 处理前导零（在 Base58 中表示为 '1'）
    int zeros = 0;
    while (zeros < input.length && input[zeros] == 0) {
      ++zeros;
    }

    // 将字节数组转换为大整数，然后进行 Base58 转换
    input = Arrays.copyOf(input, input.length); // 创建副本以避免修改原数组
    byte[] base58 = new byte[input.length * 2]; // 分配足够空间
    int length = 0;

    for (int i = zeros; i < input.length; ++i) {
      int carry = (input[i] & 0xFF);
      for (int j = 0; j < length || carry != 0; ++j) {
        carry += (base58[j] & 0xFF) * 256;
        base58[j] = (byte) (carry % 58);
        carry /= 58;
      }
      while (length < base58.length && base58[length] != 0) {
        length++;
      }
    }

    // 移除尾部的零
    while (length > 0 && base58[length - 1] == 0) {
      --length;
    }

    // 将结果转换为字符串
    char[] str = new char[length + zeros];
    Arrays.fill(str, 0, zeros, ALPHABET.charAt(0)); // 前导零
    for (int i = 0; i < length; ++i) {
      str[zeros + i] = ALPHABET.charAt(base58[length - 1 - i]);
    }
    return new String(str);
  }
  
  /**
   * 使用 Base58Check 编码字节数组。 Base58Check = Base58(版本 + 数据 + 校验和)
   * 
   * @param payload 要编码的数据
   * @return Base58Check 编码后的字符串
   */
  public static String base58CheckEncode(byte[] payload) {
    // 校验和是 payload 的两次 SHA256 哈希的前4个字节
    byte[] first = sha256(payload);
    byte[] second = sha256(first);
    byte[] checksum = Arrays.copyOf(second, 4);

    // 将 payload 和 checksum 合并
    byte[] data = new byte[payload.length + 4];
    System.arraycopy(payload, 0, data, 0, payload.length);
    System.arraycopy(checksum, 0, data, payload.length, 4);

    // 对合并后的数据进行 Base58 编码
    return Base58Utils.base58Encode(data);
  }

  /**
   * 使用 Bouncy Castle 的 SHA256Digest 计算 SHA256 哈希。
   * 
   * @param data 输入数据
   * @return SHA256 哈希结果
   */
  private static byte[] sha256(byte[] data) {
    Digest digest = new SHA256Digest();
    digest.update(data, 0, data.length);
    byte[] result = new byte[digest.getDigestSize()];
    digest.doFinal(result, 0);
    return result;
  }
}

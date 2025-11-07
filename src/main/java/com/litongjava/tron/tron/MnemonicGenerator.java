package com.litongjava.tron.tron;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.MnemonicUtils;

/**
 * 安全生成助记词、派生 seed、加密并演示派生 TRON 地址
 */
public class MnemonicGenerator {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static final SecureRandom secureRandom = new SecureRandom();

  /**
   * 生成 BIP39 助记词
   *
   * @param strengthBits 强度（128 -> 12 词；256 -> 24 词）。必须是 128, 160, 192, 224, 256
   *                     中的一个（32 的倍数）
   * @return 助记词字符串（空格分隔）
   */
  public static String generateMnemonic(int strengthBits) {
    if (strengthBits % 32 != 0 || strengthBits < 128 || strengthBits > 256) {
      throw new IllegalArgumentException("strengthBits must be one of 128,160,192,224,256");
    }

    int byteLen = strengthBits / 8;
    byte[] entropy = new byte[byteLen];
    secureRandom.nextBytes(entropy);

    // web3j 的 MnemonicUtils 生成助记词（使用 BIP-39 English wordlist）
    String mnemonic = MnemonicUtils.generateMnemonic(entropy);
    // 为了安全，立即清除明文 entropy（尽量减少在内存的存留）
    Arrays.fill(entropy, (byte) 0);
    return mnemonic;
  }
}

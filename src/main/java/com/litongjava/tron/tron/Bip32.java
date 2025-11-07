package com.litongjava.tron.tron;

import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.utils.Numeric;

import com.litongjava.tron.model.Web3WalletAddress;
import com.litongjava.tron.utils.Base58Utils;

/**
 * TRON HD 派生封装
 */
public class Bip32 {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static final int TRON_COIN_TYPE = 195;
  private static final int HARDENED_BIT = 0x80000000;

  /**
   * 核心封装函数：按 BIP-44 路径进行的 BIP-32 派生（BIP-39 助记词）
   *
   * @param mnemonic   BIP-39 助记词
   * @param passphrase BIP-39 口令（可为空字符串）
   * @param account    账户号（会加硬化：account'）
   * @param change     0=外部链，1=内部链
   * @param index      地址索引
   * 
   * 
   */
  public static Web3WalletAddress bip32(String mnemonic, String passphrase, int account, int change, int index) {
    // 1) seed
    byte[] seed = MnemonicUtils.generateSeed(mnemonic, passphrase == null ? "" : passphrase);

    // 2) master
    Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);

    // 3) path m/44'/195'/{account}'/{change}/{index}
    int[] path = new int[] { 44 | HARDENED_BIT, TRON_COIN_TYPE | HARDENED_BIT, account | HARDENED_BIT, change, index };

    // 4) derive 从根密钥生成某个子密钥
    Bip32ECKeyPair child = Bip32ECKeyPair.deriveKeyPair(master, path);

    // 5) pub -> TRON address 公钥
    byte[] pubUncompressed = child.getPublicKeyPoint().getEncoded(false);
    String address = generateTronAddress(pubUncompressed);

    // 6) 私钥固定为64 hex（零填充）
    String privHex = Numeric.toHexStringNoPrefixZeroPadded(child.getPrivateKey(), 64);

    String pathStr = String.format("m/44'/%d'/%d'/%d/%d", TRON_COIN_TYPE, account, change, index);
    return new Web3WalletAddress(pathStr, privHex, address);
  }

  /** 批量派生：给定起止索引 */
  public static List<Web3WalletAddress> bip32Batch(String mnemonic, String passphrase, int account, int change,
      int startIndex, int count) {
    List<Web3WalletAddress> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(bip32(mnemonic, passphrase, account, change, startIndex + i));
    }
    return list;
  }

  /** 从非压缩公钥生成 TRON Base58 地址 */
  public static String generateTronAddress(byte[] uncompressedPublicKey) {
    byte[] pubKeyNoPrefix = Arrays.copyOfRange(uncompressedPublicKey, 1, uncompressedPublicKey.length);
    byte[] hash = Hash.sha3(pubKeyNoPrefix);
    byte[] addressBytes = Arrays.copyOfRange(hash, 12, 32);

    byte[] addressWithPrefix = new byte[21];
    addressWithPrefix[0] = (byte) 0x41; // TRON 前缀
    System.arraycopy(addressBytes, 0, addressWithPrefix, 1, 20);

    return Base58Utils.base58CheckEncode(addressWithPrefix);
  }
}

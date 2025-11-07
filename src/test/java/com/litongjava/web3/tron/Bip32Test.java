package com.litongjava.web3.tron;

import org.junit.Test;

import com.litongjava.tron.model.Web3WalletAddress;
import com.litongjava.tron.tron.Bip32;

public class Bip32Test {

  @Test
  public void test() {
    // 这是一个标准的 BIP39 测试助记词，请勿用于主网
    String mnemonic = "find ostrich velvet rebuild woman surprise congress include stove weasel uncover orient";

    Web3WalletAddress bip32 = Bip32.bip32(mnemonic, "", 0, 0, 1);

    // --- 输出结果 ---
    System.out.println("Mnemonic: " + mnemonic);
    // 使用 Hex.toHexString 更健壮地处理 BigInteger
    System.out.println("Private Key: " + bip32.privateKeyHex);
    System.out.println("TRON Address: " + bip32.addressBase58);
  }

}

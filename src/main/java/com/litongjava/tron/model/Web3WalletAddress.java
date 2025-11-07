package com.litongjava.tron.model;

/** 返回结果对象 */
public class Web3WalletAddress {
  public final String path; // m/44'/195'/{account}'/{change}/{index}
  public final String privateKeyHex; // 32字节，零填充到64 hex
  public final String addressBase58; // TRON 地址（Base58Check，前缀T）

  public Web3WalletAddress(String path, String privateKeyHex, String addressBase58) {
    this.path = path;
    this.privateKeyHex = privateKeyHex;
    this.addressBase58 = addressBase58;
  }

  @Override
  public String toString() {
    return "path=" + path + ", priv=" + privateKeyHex + ", addr=" + addressBase58;
  }
}
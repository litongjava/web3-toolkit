package com.litongjava.web3.tron;

import org.junit.Test;

import com.litongjava.tron.tron.MnemonicGenerator;

public class MnemonicGeneratorTest {

  @Test
  public void test() {
    String address = MnemonicGenerator.generateMnemonic(256);
    System.out.println(address);
  }
}

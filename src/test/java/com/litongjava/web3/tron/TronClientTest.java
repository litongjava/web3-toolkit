package com.litongjava.web3.tron;

import org.junit.Test;

import com.litongjava.web3.model.TransactionInfo;

public class TronClientTest {

  @Test
  public void test() {
    // EnvUtils.set(TrongridConsts.BASE_URL_KEY, "https://api.shasta.trongrid.io");
    TransactionInfo transaction = TronClient.getTransactionByAddress("TUEZSdKsoDHQMeZwihtdoBiN46zxhGWYdH", 1, null);
    if (transaction != null) {
      System.out.println(transaction.getTxID());
    }
  }
}

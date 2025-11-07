package com.litongjava.web3.tron;

import java.math.BigDecimal;

import org.junit.Test;

import com.litongjava.tron.model.TransactionInfo;
import com.litongjava.tron.tron.TronClient;

public class TronClientTest {

  @Test
  public void test() {
    // EnvUtils.set(TrongridConsts.BASE_URL_KEY, "https://api.shasta.trongrid.io");
    BigDecimal bigDecimal = new BigDecimal(2.5d);
    TransactionInfo transaction = TronClient.getTransactionByAddress("TUEZSdKsoDHQMeZwihtdoBiN46zxhGWYdH", bigDecimal, null);
    if (transaction != null) {
      System.out.println(transaction.getTxID());
    }
  }
}

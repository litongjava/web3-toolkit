package com.litongjava.web3.model;

import lombok.Data;

/**
 * 交易信息实体
 */
@Data
public class TransactionInfo {
  private String txID;
  private long amountSun;
  private double amountTRX;
  private String contractRet;
}

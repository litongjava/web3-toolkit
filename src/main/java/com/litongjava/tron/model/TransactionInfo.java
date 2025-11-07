package com.litongjava.tron.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 交易信息实体
 */
@Data
public class TransactionInfo {
  private String txID;
  private long amountSun;
  private BigDecimal amountTRX;
  private String contractRet;
}

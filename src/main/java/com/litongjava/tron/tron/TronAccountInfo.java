package com.litongjava.tron.tron;

import lombok.Data;

@Data
public class TronAccountInfo {
  private String address;
  private double balanceTRX; // 单位 TRX
  private long energy;
  private long bandwidth;
}

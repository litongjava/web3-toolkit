// ==================== 预估金额请求实体 ====================

package com.litongjava.itron.model;

/**
 * 预估订单金额请求实体
 */
public class OrderPriceRequest {
  /**
   * 租赁周期（必填） 1H: 1小时 1D: 1天 3D: 3天 30D: 30天
   */
  private String period;

  /**
   * 预估能量（可选） 最少10000 如果不填写，需要填写 toAddress
   */
  private Integer energyAmount;

  /**
   * 转账的目的地址（可选） 如果不知道需要多少能量，可以不写 energyAmount 直接写转账的目的地址，系统自动判断转USDT一笔需要多少能量
   */
  private String toAddress;

  public OrderPriceRequest() {
  }

  /**
   * 构造函数 - 使用能量数量
   */
  public OrderPriceRequest(String period, Integer energyAmount) {
    this.period = period;
    this.energyAmount = energyAmount;
  }

  /**
   * 构造函数 - 使用目标地址
   */
  public OrderPriceRequest(String period, String toAddress) {
    this.period = period;
    this.toAddress = toAddress;
  }

  public OrderPriceRequest(String period, Integer energyAmount, String toAddress) {
    this.period = period;
    this.energyAmount = energyAmount;
    this.toAddress = toAddress;
  }

  // Getters and Setters
  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public Integer getEnergyAmount() {
    return energyAmount;
  }

  public void setEnergyAmount(Integer energyAmount) {
    this.energyAmount = energyAmount;
  }

  public String getToAddress() {
    return toAddress;
  }

  public void setToAddress(String toAddress) {
    this.toAddress = toAddress;
  }

  @Override
  public String toString() {
    return "OrderPriceRequest{" + "period='" + period + '\'' + ", energyAmount=" + energyAmount + ", toAddress='"
        + toAddress + '\'' + '}';
  }
}

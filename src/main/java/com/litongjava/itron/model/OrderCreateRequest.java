// ==================== 请求实体类 ====================

/**
 * 创建订单请求实体
 */
package com.litongjava.itron.model;

public class OrderCreateRequest {
  /**
   * 所需能量（必填）
   */
  private Integer energyAmount;
  
  /**
   * 租赁周期（必填）
   * 1H: 1小时
   * 1D: 1天
   * 3D: 3天
   * 30D: 30天
   */
  private String period;
  
  /**
   * 接收地址（必填）
   * 地址需要是已激活的，否则下单失败
   */
  private String receiveAddress;
  
  /**
   * 地址未激活时，无法委托能量
   * 如果参数大于0，尝试先激活，激活费用1.5TRX
   */
  private Integer active;
  
  /**
   * 当此值为1时，会先解除已存在的小额委托，再委托当前能量
   */
  private Integer exclusive;
  
  /**
   * 委托成功将向此地址发送通知
   */
  private String callbackUrl;
  
  /**
   * 外部订单号
   * 回调时将带上此信息，方便接收方关联订单
   */
  private String outTradeNo;

  public OrderCreateRequest() {
  }

  public OrderCreateRequest(Integer energyAmount, String period, String receiveAddress) {
    this.energyAmount = energyAmount;
    this.period = period;
    this.receiveAddress = receiveAddress;
  }

  // Getters and Setters
  public Integer getEnergyAmount() {
    return energyAmount;
  }

  public void setEnergyAmount(Integer energyAmount) {
    this.energyAmount = energyAmount;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getReceiveAddress() {
    return receiveAddress;
  }

  public void setReceiveAddress(String receiveAddress) {
    this.receiveAddress = receiveAddress;
  }

  public Integer getActive() {
    return active;
  }

  public void setActive(Integer active) {
    this.active = active;
  }

  public Integer getExclusive() {
    return exclusive;
  }

  public void setExclusive(Integer exclusive) {
    this.exclusive = exclusive;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public String getOutTradeNo() {
    return outTradeNo;
  }

  public void setOutTradeNo(String outTradeNo) {
    this.outTradeNo = outTradeNo;
  }

  @Override
  public String toString() {
    return "OrderCreateRequest{" +
        "energyAmount=" + energyAmount +
        ", period='" + period + '\'' +
        ", receiveAddress='" + receiveAddress + '\'' +
        ", active=" + active +
        ", exclusive=" + exclusive +
        ", callbackUrl='" + callbackUrl + '\'' +
        ", outTradeNo='" + outTradeNo + '\'' +
        '}';
  }
}

// ==================== 响应实体类 ====================

/**
 * 创建订单响应实体
 */

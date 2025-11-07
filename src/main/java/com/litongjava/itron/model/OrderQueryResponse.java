package com.litongjava.itron.model;

import java.util.List;

public class OrderQueryResponse {
  /**
   * 错误码，为0时正常
   */
  private Integer errno;
  
  /**
   * 错误原因，errno > 0 时有
   */
  private String message;
  
  /**
   * 接收能量的地址
   */
  private String receive_address;
  
  /**
   * 订单号，同serial
   */
  private String order_no;
  
  /**
   * 下单的能量
   */
  private Integer energy_amount;
  
  /**
   * 已委托的能量
   */
  private Double pay_amount;
  
  /**
   * 支付的TRX，单位sun
   */
  private Long amount;
  
  /**
   * 委托详情列表
   * 大额订单可能拆分多次委托
   */
  private List<DelegateDetail> details;
  
  /**
   * 下单时间
   */
  private String create_time;
  
  /**
   * 区分哪个API下的单
   */
  private String api_name;
  
  /**
   * 租用时长
   * 0: 1小时
   * 1: 1天
   * 3: 3天
   * 30: 30天
   */
  private Integer period;
  
  /**
   * 订单状态
   * 0: 超时关闭
   * 10: 等待支付
   * 20: 已支付
   * 30: 委托准备中
   * 31: 部分委托
   * 32: 异常重试中
   * 40: 正常完成
   * 41: 退款终止
   * 43: 异常终止
   */
  private Integer status;
  
  /**
   * 退款金额，单位sun
   */
  private Long refund_amount;

  public OrderQueryResponse() {
  }

  public boolean isSuccess() {
    return errno != null && errno == 0;
  }

  /**
   * 获取订单状态描述
   */
  public String getStatusDesc() {
    if (status == null) return "未知";
    switch (status) {
      case 0: return "超时关闭";
      case 10: return "等待支付";
      case 20: return "已支付";
      case 30: return "委托准备中";
      case 31: return "部分委托";
      case 32: return "异常重试中";
      case 40: return "正常完成";
      case 41: return "退款终止";
      case 43: return "异常终止";
      default: return "未知状态";
    }
  }

  /**
   * 获取TRX格式的订单金额
   */
  public Double getAmountInTrx() {
    return amount != null ? amount / 1000000.0 : null;
  }

  /**
   * 获取TRX格式的退款金额
   */
  public Double getRefundAmountInTrx() {
    return refund_amount != null ? refund_amount / 1000000.0 : null;
  }

  // Getters and Setters
  public Integer getErrno() {
    return errno;
  }

  public void setErrno(Integer errno) {
    this.errno = errno;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getReceiveAddress() {
    return receive_address;
  }

  public void setReceiveAddress(String receiveAddress) {
    this.receive_address = receiveAddress;
  }

  public String getOrderNo() {
    return order_no;
  }

  public void setOrderNo(String orderNo) {
    this.order_no = orderNo;
  }

  public Integer getEnergyAmount() {
    return energy_amount;
  }

  public void setEnergyAmount(Integer energyAmount) {
    this.energy_amount = energyAmount;
  }

  public Double getPayAmount() {
    return pay_amount;
  }

  public void setPayAmount(Double payAmount) {
    this.pay_amount = payAmount;
  }

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public List<DelegateDetail> getDetails() {
    return details;
  }

  public void setDetails(List<DelegateDetail> details) {
    this.details = details;
  }

  public String getCreateTime() {
    return create_time;
  }

  public void setCreateTime(String createTime) {
    this.create_time = createTime;
  }

  public String getApiName() {
    return api_name;
  }

  public void setApiName(String apiName) {
    this.api_name = apiName;
  }

  public Integer getPeriod() {
    return period;
  }

  public void setPeriod(Integer period) {
    this.period = period;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Long getRefundAmount() {
    return refund_amount;
  }

  public void setRefundAmount(Long refundAmount) {
    this.refund_amount = refundAmount;
  }

  @Override
  public String toString() {
    return "OrderQueryResponse{" +
        "errno=" + errno +
        ", message='" + message + '\'' +
        ", receiveAddress='" + receive_address + '\'' +
        ", orderNo='" + order_no + '\'' +
        ", energyAmount=" + energy_amount +
        ", payAmount=" + pay_amount +
        ", amount=" + amount +
        ", details=" + details +
        ", createTime='" + create_time + '\'' +
        ", apiName='" + api_name + '\'' +
        ", period=" + period +
        ", status=" + status +
        ", refundAmount=" + refund_amount +
        '}';
  }
}

/**
 * 委托详情实体
 */

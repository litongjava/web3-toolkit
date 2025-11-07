package com.litongjava.itron.model;

public class OrderCreateResponse {
  /**
   * 错误码，为0时正常
   */
  private Integer errno;
  
  /**
   * 错误原因，errno > 0 时有
   */
  private String message;
  
  /**
   * 内部订单号
   */
  private String serial;
  
  /**
   * 订单消耗，单位sun
   */
  private Long amount;
  
  /**
   * 余额，单位sun，转成TRX需要除以1000000
   */
  private Long balance;

  public OrderCreateResponse() {
  }

  public boolean isSuccess() {
    return errno != null && errno == 0;
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

  public String getSerial() {
    return serial;
  }

  public void setSerial(String serial) {
    this.serial = serial;
  }

  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public Long getBalance() {
    return balance;
  }

  public void setBalance(Long balance) {
    this.balance = balance;
  }

  /**
   * 获取TRX格式的余额
   */
  public Double getBalanceInTrx() {
    return balance != null ? balance / 1000000.0 : null;
  }

  /**
   * 获取TRX格式的订单金额
   */
  public Double getAmountInTrx() {
    return amount != null ? amount / 1000000.0 : null;
  }

  @Override
  public String toString() {
    return "OrderCreateResponse{" +
        "errno=" + errno +
        ", message='" + message + '\'' +
        ", serial='" + serial + '\'' +
        ", amount=" + amount +
        ", balance=" + balance +
        '}';
  }
}
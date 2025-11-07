package com.litongjava.itron.model;

public class DelegateDetail {
  /**
   * 委托交易哈希
   */
  private String delegateHash;
  
  /**
   * 委托时间
   */
  private String delegateTime;
  
  /**
   * 回收交易哈希
   */
  private String reclaimHash;
  
  /**
   * 预计回收时间
   */
  private String reclaimTime;
  
  /**
   * 实际回收时间
   */
  private String reclaimTimeReal;
  
  /**
   * 状态
   * 20: 委托中
   * 30: 已回收
   */
  private Integer status;

  public DelegateDetail() {
  }

  /**
   * 获取状态描述
   */
  public String getStatusDesc() {
    if (status == null) return "未知";
    switch (status) {
      case 20: return "委托中";
      case 30: return "已回收";
      default: return "未知状态";
    }
  }

  // Getters and Setters
  public String getDelegateHash() {
    return delegateHash;
  }

  public void setDelegateHash(String delegateHash) {
    this.delegateHash = delegateHash;
  }

  public String getDelegateTime() {
    return delegateTime;
  }

  public void setDelegateTime(String delegateTime) {
    this.delegateTime = delegateTime;
  }

  public String getReclaimHash() {
    return reclaimHash;
  }

  public void setReclaimHash(String reclaimHash) {
    this.reclaimHash = reclaimHash;
  }

  public String getReclaimTime() {
    return reclaimTime;
  }

  public void setReclaimTime(String reclaimTime) {
    this.reclaimTime = reclaimTime;
  }

  public String getReclaimTimeReal() {
    return reclaimTimeReal;
  }

  public void setReclaimTimeReal(String reclaimTimeReal) {
    this.reclaimTimeReal = reclaimTimeReal;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "DelegateDetail{" +
        "delegateHash='" + delegateHash + '\'' +
        ", delegateTime='" + delegateTime + '\'' +
        ", reclaimHash='" + reclaimHash + '\'' +
        ", reclaimTime='" + reclaimTime + '\'' +
        ", reclaimTimeReal='" + reclaimTimeReal + '\'' +
        ", status=" + status +
        '}';
  }
}
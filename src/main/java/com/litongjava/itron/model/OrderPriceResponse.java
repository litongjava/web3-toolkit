package com.litongjava.itron.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预估订单金额响应实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPriceResponse {
  /**
   * 错误码，为0时正常
   */
  private int errno;

  /**
   * 错误原因，errno > 0 时有
   */
  private String message;

  /**
   * 租赁周期
   */
  private String period;

  /**
   * 能量数量
   */
  private Integer energy_amount;

  /**
   * 单价，单位sun
   */
  private Long price;

  /**
   * 需要支付的TRX总价，单位sun
   */
  private Long total_price;

  /**
   * 小额手续费（小于50000能量需要），单位sun
   */
  private Long addition;

  /**
   * 判断请求是否成功
   */
  public boolean isSuccess() {
    return errno == 0;
  }

  /**
   * 获取TRX格式的单价
   */
  public Double getPriceInTrx() {
    return price != null ? price / 1000000.0 : null;
  }

  /**
   * 获取TRX格式的总价
   */
  public Double getTotalPriceInTrx() {
    return total_price != null ? total_price / 1000000.0 : null;
  }

  /**
   * 获取TRX格式的手续费
   */
  public Double getAdditionInTrx() {
    return addition != null ? addition / 1000000.0 : null;
  }
}
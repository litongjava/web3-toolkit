package com.litongjava.itron;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.litongjava.itron.model.OrderCreateRequest;
import com.litongjava.itron.model.OrderCreateResponse;
import com.litongjava.itron.model.OrderPriceRequest;
import com.litongjava.itron.model.OrderPriceResponse;
import com.litongjava.itron.model.OrderQueryResponse;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.OkHttpClientPool;
import com.litongjava.tio.utils.json.JsonUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ITRX.IO API Client 用于与 ITRX.IO 能量租赁平台进行交互
 * 
 * @author Tong Li
 */
public class ItrxIOClient {
  public static final String BASE_URL = "https://itrx.io";
  public static final String ORDER_CREATE_ENDPOINT = "/api/v1/frontend/order";
  public static final String ORDER_QUERY_ENDPOINT = "/api/v1/frontend/order/query";
  public static final String ORDER_PRICE_ENDPOINT = "/api/v1/frontend/order/price";

  private final String apiKey;
  private final String apiSecret;
  private final OkHttpClient client = OkHttpClientPool.get120HttpClient();

  /**
   * 构造函数，从环境变量读取配置
   */
  public ItrxIOClient() {
    this.apiKey = EnvUtils.getStr("ITRX_API_KEY");
    this.apiSecret = EnvUtils.getStr("ITRX_API_SECRET");
    if (this.apiKey == null || this.apiSecret == null) {
      throw new IllegalStateException("ITRX_API_KEY and ITRX_API_SECRET must be set in environment variables");
    }
  }

  /**
   * 构造函数，手动指定 API Key 和 Secret
   * 
   * @param apiKey    API Key
   * @param apiSecret API Secret
   */
  public ItrxIOClient(String apiKey, String apiSecret) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
  }

  /**
   * 创建订单（使用实体类）
   * 
   * @param request 订单请求实体
   * @return 订单响应实体
   * @throws Exception 请求异常
   */
  public OrderCreateResponse createOrder(OrderCreateRequest request) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("energy_amount", request.getEnergyAmount());
    params.put("period", request.getPeriod());
    params.put("receive_address", request.getReceiveAddress());

    if (request.getActive() != null) {
      params.put("active", request.getActive());
    }
    if (request.getExclusive() != null) {
      params.put("exclusive", request.getExclusive());
    }
    if (request.getCallbackUrl() != null && !request.getCallbackUrl().isEmpty()) {
      params.put("callback_url", request.getCallbackUrl());
    }
    if (request.getOutTradeNo() != null && !request.getOutTradeNo().isEmpty()) {
      params.put("out_trade_no", request.getOutTradeNo());
    }

    String result = createOrderInternal(params);
    return JsonUtils.parse(result, OrderCreateResponse.class);
  }

  /**
   * 创建订单
   * 
   * @param energyAmount   所需能量
   * @param period         租赁周期 (1H/1D/3D/30D)
   * @param receiveAddress 接收地址
   * @return 订单响应
   * @throws Exception 请求异常
   */
  public OrderCreateResponse createOrder(int energyAmount, String period, String receiveAddress) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("energy_amount", energyAmount);
    params.put("period", period);
    params.put("receive_address", receiveAddress);

    String result = createOrderInternal(params);
    return JsonUtils.parse(result, OrderCreateResponse.class);
  }

  /**
   * 创建订单（完整参数）
   * 
   * @param energyAmount   所需能量
   * @param period         租赁周期 (1H/1D/3D/30D)
   * @param receiveAddress 接收地址
   * @param callbackUrl    回调地址
   * @param outTradeNo     外部订单号
   * @return 订单响应
   * @throws Exception 请求异常
   */
  public OrderCreateResponse createOrder(int energyAmount, String period, String receiveAddress, String callbackUrl,
      String outTradeNo) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("energy_amount", energyAmount);
    params.put("period", period);
    params.put("receive_address", receiveAddress);

    if (callbackUrl != null && !callbackUrl.isEmpty()) {
      params.put("callback_url", callbackUrl);
    }
    if (outTradeNo != null && !outTradeNo.isEmpty()) {
      params.put("out_trade_no", outTradeNo);
    }

    String result = createOrderInternal(params);
    return JsonUtils.parse(result, OrderCreateResponse.class);
  }

  /**
   * 创建订单（内部方法）
   * 
   * @param params 订单参数
   * @return 订单响应
   * @throws Exception 请求异常
   */
  private String createOrderInternal(Map<String, Object> params) throws Exception {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());

    // 按 key 字典顺序排序
    TreeMap<String, Object> sortedParams = new TreeMap<>(params);
    String jsonData = JsonUtils.toJson(sortedParams);

    // 生成签名
    String message = timestamp + "&" + jsonData;
    String signature = generateSignature(message);

    // 构建请求
    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(jsonData, mediaType);

    Request request = new Request.Builder().url(BASE_URL + ORDER_CREATE_ENDPOINT).method("POST", body)
        .addHeader("API-KEY", apiKey).addHeader("TIMESTAMP", timestamp).addHeader("SIGNATURE", signature)
        .addHeader("Content-Type", "application/json").build();

    // 发送请求
    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      return responseBody;
    }
  }

  /**
   * 查询订单（通过内部订单号）
   * 
   * @param serial 内部订单号
   * @return 订单详情
   * @throws Exception 请求异常
   */
  public OrderQueryResponse queryOrderBySerial(String serial) throws Exception {
    String url = BASE_URL + ORDER_QUERY_ENDPOINT + "?serial=" + serial;
    return queryOrder(url);
  }

  /**
   * 查询订单（通过外部订单号）
   * 
   * @param outTradeNo 外部订单号
   * @return 订单详情
   * @throws Exception 请求异常
   */
  public OrderQueryResponse queryOrderByOutTradeNo(String outTradeNo) throws Exception {
    String url = BASE_URL + ORDER_QUERY_ENDPOINT + "?out_trade_no=" + outTradeNo;
    return queryOrder(url);
  }

  /**
   * 查询订单（内部方法）
   * 
   * @param url 请求 URL
   * @return 订单详情
   * @throws Exception 请求异常
   */
  private OrderQueryResponse queryOrder(String url) throws Exception {
    Request request = new Request.Builder().url(url).addHeader("API-KEY", apiKey).get().build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      return JsonUtils.parse(responseBody, OrderQueryResponse.class);
    }
  }

  /**
   * 生成 HMAC-SHA256 签名
   * 
   * @param data 待签名数据
   * @return 签名字符串
   * @throws Exception 签名异常
   */
  private String generateSignature(String data) throws Exception {
    Mac sha256Hmac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    sha256Hmac.init(secretKey);

    byte[] bytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    StringBuilder hash = new StringBuilder();

    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hash.append('0');
      }
      hash.append(hex);
    }

    return hash.toString();
  }

  /**
   * 预估订单金额（使用实体类）
   * 
   * @param request 预估请求实体
   * @return 预估响应实体
   * @throws Exception 请求异常
   */
  public OrderPriceResponse getOrderPrice(OrderPriceRequest request) throws Exception {
    StringBuilder urlBuilder = new StringBuilder(BASE_URL + ORDER_PRICE_ENDPOINT);
    urlBuilder.append("?period=").append(request.getPeriod());

    if (request.getEnergyAmount() != null) {
      urlBuilder.append("&energy_amount=").append(request.getEnergyAmount());
    }

    if (request.getToAddress() != null && !request.getToAddress().isEmpty()) {
      urlBuilder.append("&to_address=").append(request.getToAddress());
    }

    return getOrderPriceInternal(urlBuilder.toString());
  }

  /**
   * 预估订单金额（通过能量数量）
   * 
   * @param period       租赁周期 (1H/1D/3D/30D)
   * @param energyAmount 能量数量（最少10000）
   * @return 预估响应实体
   * @throws Exception 请求异常
   */
  public OrderPriceResponse getOrderPrice(String period, int energyAmount) throws Exception {
    String url = BASE_URL + ORDER_PRICE_ENDPOINT + "?period=" + period + "&energy_amount=" + energyAmount;
    return getOrderPriceInternal(url);
  }

  /**
   * 预估订单金额（通过目标地址） 系统自动判断转USDT一笔需要多少能量
   * 
   * @param period    租赁周期 (1H/1D/3D/30D)
   * @param toAddress 转账的目的地址
   * @return 预估响应实体
   * @throws Exception 请求异常
   */
  public OrderPriceResponse getOrderPriceByAddress(String period, String toAddress) throws Exception {
    String url = BASE_URL + ORDER_PRICE_ENDPOINT + "?period=" + period + "&to_address=" + toAddress;
    return getOrderPriceInternal(url);
  }

  /**
   * 预估订单金额（内部方法）
   * 
   * @param url 请求 URL
   * @return 预估响应实体
   * @throws Exception 请求异常
   */
  private OrderPriceResponse getOrderPriceInternal(String url) throws Exception {
    Request request = new Request.Builder().url(url).addHeader("API-KEY", apiKey).get().build();

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body().string();
      return JsonUtils.parse(responseBody, OrderPriceResponse.class);
    }
  }

}
package com.litongjava.web3.tron;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.litongjava.model.http.response.ResponseVo;
import com.litongjava.tio.utils.http.HttpUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

@Slf4j
public class TronClient {

  public static final String BASE_URL = "https://api.trongrid.io";

  /**
   * 获取账户余额和资源
   */
  public static TronAccountInfo getAccountInfo(String address, String apiKey) {
    TronAccountInfo accountInfo = new TronAccountInfo();

    ResponseVo responseVo = account(address, apiKey);

    if (!responseVo.isOk()) {
      throw new RuntimeException("获取余额失败: " + responseVo.getCode());
    }
    String body = responseVo.getBodyString();
    JSONObject obj = JSON.parseObject(body);
    JSONArray arr = obj.getJSONArray("data");
    if (arr != null && !arr.isEmpty()) {
      JSONObject acc = arr.getJSONObject(0);
      long balanceSun = acc.getLongValue("balance");
      accountInfo.setBalanceTRX(balanceSun / 1_000_000.0);
    } else {
      accountInfo.setBalanceTRX(0.0);
    }

    responseVo = accountResources(address, apiKey);

    if (!responseVo.isOk()) {
      throw new RuntimeException("获取资源失败: " + responseVo.getCode());
    }
    body = responseVo.getBodyString();
    JSONObject res  = JSON.parseObject(body);
    if (obj != null) {
      long freeNetLimit = res.getLongValue("freeNetLimit");
      long freeNetUsed = res.getLongValue("freeNetUsed");
      long netLimit = res.getLongValue("netLimit");
      long netUsed = res.getLongValue("netUsed");
      long energyLimit = res.getLongValue("EnergyLimit");
      long energyUsed = res.getLongValue("EnergyUsed");

      long bandwidth = Math.max(0, (freeNetLimit - freeNetUsed)) + Math.max(0, (netLimit - netUsed));
      long energy = Math.max(0, (energyLimit - energyUsed));

      accountInfo.setBandwidth(bandwidth);
      accountInfo.setEnergy(energy);
    }

    accountInfo.setAddress(address);
    return accountInfo;
  }


  public static ResponseVo account(String address, String apiKey) {
    // 1) 账户余额
    String urlAcc = BASE_URL + "/v1/accounts/" + address;
    Request.Builder builderAcc = new Request.Builder().url(urlAcc).get();

    if (apiKey != null && !apiKey.isEmpty()) {
      builderAcc.addHeader("TRON-PRO-API-KEY", apiKey);
    }
    Request request = builderAcc.build();
    return HttpUtils.call(request);
  }
  
  public static ResponseVo accountResources(String address, String apiKey) {
    // TronGrid 代理的 JavaTron 接口（主网）
    String url = BASE_URL + "/wallet/getaccountresource";

    // Tron 要求：Base58 地址时 visible=true；若用 0x/Hex 则 visible=false 并传 Hex
    com.alibaba.fastjson2.JSONObject payload = new com.alibaba.fastjson2.JSONObject();
    payload.put("address", address);
    payload.put("visible", true);

    okhttp3.RequestBody body = okhttp3.RequestBody.create(payload.toJSONString(),
        okhttp3.MediaType.parse("application/json; charset=utf-8"));

    okhttp3.Request.Builder rb = new okhttp3.Request.Builder().url(url).post(body);
    if (apiKey != null && !apiKey.isEmpty()) {
      rb.addHeader("TRON-PRO-API-KEY", apiKey);
    }
    return com.litongjava.tio.utils.http.HttpUtils.call(rb.build());
  }
}

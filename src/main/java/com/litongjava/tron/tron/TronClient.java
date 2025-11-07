package com.litongjava.tron.tron;

import java.math.BigDecimal;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.litongjava.model.http.response.ResponseVo;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.HttpUtils;
import com.litongjava.tron.consts.TrongridConsts;
import com.litongjava.tron.model.TransactionInfo;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

@Slf4j
public class TronClient {

  public static final String BASE_URL = EnvUtils.getStr(TrongridConsts.BASE_URL_KEY, TrongridConsts.BASE_URL);

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
    JSONObject res = JSON.parseObject(body);
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
    return HttpUtils.call(rb.build());
  }

  /**
   * Check if the specified wallet address has a successful transaction with the
   * given amount.
   *
   * @param walletAddress the wallet address (Base58)
   * @param amountTRX     the transaction amount (in TRX, not SUN)
   * @param apiKey        optional TRON-PRO-API-KEY, can be null
   * @return TransactionInfo if at least one matching SUCCESS transaction is
   *         found, otherwise null
   */
  public static TransactionInfo getTransactionByAddress(String walletAddress, BigDecimal amountTRX, String apiKey) {

    String url = BASE_URL + "/v1/accounts/" + walletAddress + "/transactions";
    Request.Builder builder = new Request.Builder().url(url).get();
    if (apiKey == null) {
      apiKey = EnvUtils.getStr(TrongridConsts.TRONGRID_API_KEY);
    }
    if (apiKey != null && !apiKey.isEmpty()) {
      builder.addHeader("TRON-PRO-API-KEY", apiKey);
    }
    Request request = builder.build();

    try {
      ResponseVo resp = HttpUtils.call(request);
      if (!resp.isOk()) {
        log.error("HTTP request failed, code={},{}", resp.getCode(), resp.getBodyString());
        return null;
      }

      String body = resp.getBodyString();
      JSONObject json = JSON.parseObject(body);
      JSONArray data = json.getJSONArray("data");
      if (data == null || data.isEmpty()) {
        return null;
      }

      long targetAmountSun = amountTRX.multiply(BigDecimal.valueOf(1_000_000L)).longValue();

      for (int i = 0; i < data.size(); i++) {
        JSONObject tx = data.getJSONObject(i);

        // 1. Check status SUCCESS
        JSONArray retArr = tx.getJSONArray("ret");
        if (retArr == null || retArr.isEmpty()) {
          continue;
        }

        String contractRet = retArr.getJSONObject(0).getString("contractRet");
        if (!"SUCCESS".equalsIgnoreCase(contractRet)) {
          continue;
        }

        // 2. Parse contract value
        JSONObject raw = tx.getJSONObject("raw_data");
        if (raw == null) {
          continue;
        }
        JSONArray contractArr = raw.getJSONArray("contract");
        if (contractArr == null || contractArr.isEmpty()) {
          continue;
        }
        JSONObject contract = contractArr.getJSONObject(0);
        JSONObject parameter = contract.getJSONObject("parameter");
        if (parameter == null) {
          continue;
        }
        JSONObject value = parameter.getJSONObject("value");
        if (value == null) {
          continue;
        }

        long amountSun = value.getLongValue("amount");

        // 3. Compare target
        if (amountSun == targetAmountSun) {
          String txID = tx.getString("txID");
          TransactionInfo info = new TransactionInfo();
          info.setTxID(txID);
          info.setAmountSun(amountSun);
          info.setAmountTRX(amountTRX);
          info.setContractRet(contractRet);
          return info;
        }
      }

      log.info("No matching SUCCESS transaction found for amount={} TRX", amountTRX);
      return null;
    } catch (Exception e) {
      log.error("Error while checking transaction", e);
      return null;
    }
  }

}

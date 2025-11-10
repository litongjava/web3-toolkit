package com.litongjava.tron.tron;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.litongjava.model.http.response.ResponseVo;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.HttpUtils;
import com.litongjava.tron.consts.TrongridConsts;
import com.litongjava.tron.model.TronRawData;
import com.litongjava.tron.model.TronTransaction;
import com.litongjava.tron.model.TronTransactionInfo;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

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

    RequestBody body = RequestBody.create(payload.toJSONString(), MediaType.parse("application/json; charset=utf-8"));

    Request.Builder rb = new Request.Builder().url(url).post(body);
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
  public static TronTransactionInfo getTransactionByAddress(String walletAddress, BigDecimal amountTRX, String apiKey) {

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
          TronTransactionInfo info = new TronTransactionInfo();
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

  public static String transferTrx(String fromAddress, String toAddress, long amountSun, String privateKeyHex) {
    return transferTrx(fromAddress, toAddress, amountSun, privateKeyHex, null);
  }

  public static String transferTrx(String fromAddress, String toAddress, long amountSun, String privateKeyHex, String apiKey) {
    if (apiKey == null || apiKey.isEmpty()) {
      apiKey = EnvUtils.getStr(TrongridConsts.TRONGRID_API_KEY);
    }
    try {
      ResponseVo createResp = createTransaction(fromAddress, toAddress, amountSun, apiKey);
      if (!createResp.isOk()) {
        log.error("createtransaction failed: code={}, body={}", createResp.getCode(), createResp.getBodyString());
        return null;
      }
      TronTransaction unsignedTx = JSON.parseObject(createResp.getBodyString(), TronTransaction.class);
      String raw_data_hex = unsignedTx.getRaw_data_hex();
      if (raw_data_hex == null) {
        log.error("invalid unsigned tx: {}", createResp.getBodyString());
        return null;
      }

      // 2) 计算 txID = SHA256(raw_data)
      byte[] rawData = Hex.decode(raw_data_hex);
      byte[] txIdBytes = sha256(rawData);
      String txIdHex = Hex.toHexString(txIdBytes);

      // 3) 使用 secp256k1 对 txID 做签名，得到 r||s||v (65字节)
      byte[] privKeyBytes = Hex.decode(privateKeyHex);
      String sigHex = signTron(txIdBytes, privKeyBytes); // r(32)+s(32)+v(1) => hex

      TronRawData raw_data = unsignedTx.getRaw_data();

      ResponseVo bcResp = broadcastTransaction(txIdHex, raw_data, raw_data_hex, sigHex, apiKey);
      String bodyString = bcResp.getBodyString();
      if (!bcResp.isOk()) {
        log.error("broadcasttransaction failed: code={}, body={}", bcResp.getCode(), bodyString);
        return null;
      }
      JSONObject bc = JSON.parseObject(bodyString);
      boolean result = bc.getBooleanValue("result");
      String txid = bc.getString("txid");
      if (!result || txid == null) {
        log.error("broadcast failed: {}", bodyString);
        return null;
      }
      return txid;
    } catch (Exception e) {
      log.error("transferTrx local-sign error", e);
      return null;
    }
  }

  public static ResponseVo broadcastTransaction(String txIdHex, TronRawData raw_data, String raw_data_hex, String sigHex, String apiKey) {
    // 4) 组装广播交易对象
    JSONObject toBroadcast = new JSONObject();
    toBroadcast.put("visible", true);
    toBroadcast.put("txID", txIdHex);
    toBroadcast.put("raw_data", raw_data);
    toBroadcast.put("raw_data_hex", raw_data_hex);
    // Tron 要求 signature 是数组
    toBroadcast.put("signature", new JSONArray() {
      {
        add(sigHex);
      }
    });

    String jsonString = toBroadcast.toJSONString();
    RequestBody bodyBroadcast = RequestBody.create(jsonString, MediaType.parse("application/json; charset=utf-8"));
    Request.Builder rbBroadcast = new Request.Builder().url(BASE_URL + "/wallet/broadcasttransaction").post(bodyBroadcast);
    if (apiKey != null && !apiKey.isEmpty()) {
      rbBroadcast.addHeader("TRON-PRO-API-KEY", apiKey);
    }
    ResponseVo bcResp = HttpUtils.call(rbBroadcast.build());
    return bcResp;
  }

  public static ResponseVo createTransaction(String fromAddress, String toAddress, long amountSun, String apiKey) {
    // 1) 创建交易（保持与原来一致）
    JSONObject createPayload = new JSONObject();
    createPayload.put("visible", true);
    createPayload.put("to_address", toAddress);
    createPayload.put("owner_address", fromAddress);
    createPayload.put("amount", amountSun);

    String jsonString = createPayload.toJSONString();
    RequestBody bodyCreate = RequestBody.create(jsonString, MediaType.parse("application/json; charset=utf-8"));
    Request.Builder rbCreate = new Request.Builder().url(BASE_URL + "/wallet/createtransaction").post(bodyCreate);
    if (apiKey != null && !apiKey.isEmpty()) {
      rbCreate.addHeader("TRON-PRO-API-KEY", apiKey);
    }
    ResponseVo createResp = HttpUtils.call(rbCreate.build());
    return createResp;
  }

  /** 对输入做单轮 SHA-256 */
  private static byte[] sha256(byte[] input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return md.digest(input);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Tron 签名：对 txID 做 ECDSA(secp256k1) 签名，输出 r||s||v（v=27+recId）
   * 参考：ECDSA(secp256k1) + SHA-256；签名放在 signature[0] 以 hex 拼接 r,s,v。
   */
  private static String signTron(byte[] hash32, byte[] privKey) {
    // secp256k1 曲线与参数
    var params = org.bouncycastle.crypto.ec.CustomNamedCurves.getByName("secp256k1");
    var domain = new org.bouncycastle.crypto.params.ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());

    // RFC6979 确定性 k
    ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    BigInteger d = new BigInteger(1, privKey);
    signer.init(true, new ECPrivateKeyParameters(d, domain));

    BigInteger[] rs = signer.generateSignature(hash32);
    BigInteger r = rs[0];
    BigInteger s = rs[1];

    // 规范化 s 到低 s（低于 n/2）
    BigInteger halfN = params.getN().shiftRight(1);
    if (s.compareTo(halfN) > 0) {
      s = params.getN().subtract(s);
    }

    // 计算 recovery id（0/1/2/3），以恢复公钥判断
    int recId = calcRecId(hash32, r, s, d, params);

    // v = 27 + recId
    int v = 27 + recId;

    byte[] rBytes = toFixed(r, 32);
    byte[] sBytes = toFixed(s, 32);
    byte[] sig65 = new byte[65];
    System.arraycopy(rBytes, 0, sig65, 0, 32);
    System.arraycopy(sBytes, 0, sig65, 32, 32);
    sig65[64] = (byte) v;

    return Hex.toHexString(sig65);
  }

  /** 计算 recovery id：遍历 0..3 尝试恢复公钥并匹配 */
  private static int calcRecId(byte[] hash32, BigInteger r, BigInteger s, BigInteger d, org.bouncycastle.asn1.x9.X9ECParameters params) {

    ECPoint pub = params.getG().multiply(d).normalize();
    for (int recId = 0; recId < 4; recId++) {
      ECPoint Q = recoverFromSignature(recId, r, s, hash32, params);
      if (Q != null && Q.normalize().equals(pub)) {
        return recId;
      }
    }
    // 找不到就回退 0（通常不会）
    return 0;
  }

  /** 根据 (recId,r,s,hash) 恢复公钥（标准 secp256k1 做法） */
  private static ECPoint recoverFromSignature(int recId, BigInteger r, BigInteger s, byte[] hash,
      org.bouncycastle.asn1.x9.X9ECParameters params) {

    BigInteger n = params.getN();
    BigInteger i = BigInteger.valueOf((long) recId / 2);
    BigInteger x = r.add(i.multiply(n));

    var curve = params.getCurve();
    BigInteger prime = ((org.bouncycastle.math.ec.custom.sec.SecP256K1Curve) curve).getQ();
    if (x.compareTo(prime) >= 0)
      return null;

    ECPoint R = decompressKey(x, (recId & 1) == 1, curve);
    if (!R.multiply(n).isInfinity())
      return null;

    BigInteger e = new BigInteger(1, hash);
    BigInteger rInv = r.modInverse(n);
    BigInteger srInv = s.multiply(rInv).mod(n);
    BigInteger eNeg = n.subtract(e).multiply(rInv).mod(n);

    ECPoint Q = params.getG().multiply(eNeg).add(R.multiply(srInv)).normalize();
    return Q;
  }

  private static ECPoint decompressKey(BigInteger xBN, boolean yBit, org.bouncycastle.math.ec.ECCurve curve) {
    byte[] compEnc = Hex.decode("02" + leftPadHex(xBN.toString(16), 64));
    if (yBit)
      compEnc[0] = 0x03;
    return curve.decodePoint(compEnc);
  }

  private static byte[] toFixed(BigInteger v, int size) {
    byte[] b = v.toByteArray();
    if (b.length == size)
      return b;
    if (b.length == size + 1 && b[0] == 0)
      return Arrays.copyOfRange(b, 1, b.length);
    byte[] out = new byte[size];
    System.arraycopy(b, Math.max(0, b.length - size), out, Math.max(0, size - b.length), Math.min(size, b.length));
    return out;
  }

  private static String leftPadHex(String s, int len) {
    if (s.length() >= len)
      return s;
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len - s.length(); i++)
      sb.append('0');
    sb.append(s);
    return sb.toString();
  }

}

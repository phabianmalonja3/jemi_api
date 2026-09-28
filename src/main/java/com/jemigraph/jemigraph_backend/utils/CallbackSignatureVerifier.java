package com.jemigraph.jemigraph_backend.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class CallbackSignatureVerifier {

  public boolean verify(
      String utilityRef,
      String externalReference,
      String transactionStatus,
      String operatorName,
      String signatureBase64,
      String publicKeyPem) {
    try {
      String uRef = utilityRef != null ? utilityRef : "";
      String extRef = externalReference != null ? externalReference : "";
      String tStatus = transactionStatus != null ? transactionStatus : "";
      String opName = operatorName != null ? operatorName : "";

      String dataToVerify = uRef + extRef + tStatus + opName;
      byte[] dataBytes = dataToVerify.getBytes(StandardCharsets.UTF_8);
      byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

      String keyBase64 =
          publicKeyPem
              .replace("-----BEGIN PUBLIC KEY-----", "")
              .replace("-----END PUBLIC KEY-----", "")
              .replaceAll("\\s", "");

      byte[] keyBytes = Base64.getDecoder().decode(keyBase64);

      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory.generatePublic(keySpec);

      Signature sig = Signature.getInstance("SHA256withRSA");
      sig.initVerify(publicKey);
      sig.update(dataBytes);

      return sig.verify(signatureBytes);

    } catch (Exception e) {

      return false;
    }
  }
}

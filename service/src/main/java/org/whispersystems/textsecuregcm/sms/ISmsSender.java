package org.whispersystems.textsecuregcm.sms;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ISmsSender {

  boolean isEnabled();

  List<String> getPrefixes();

  CompletableFuture<Boolean> deliverSmsVerification(String destination, Optional<String> clientType, String verificationCode);

  CompletableFuture<Boolean> deliverVoxVerification(String destination, String verificationCode, Optional<String> locale);

}

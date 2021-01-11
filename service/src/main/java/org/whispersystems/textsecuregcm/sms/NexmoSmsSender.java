package org.whispersystems.textsecuregcm.sms;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.configuration.*;
import org.whispersystems.textsecuregcm.http.FaultTolerantHttpClient;
import org.whispersystems.textsecuregcm.http.FormDataBodyPublisher;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.ExecutorUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.codahale.metrics.MetricRegistry.name;

public class NexmoSmsSender implements ISmsSender {

  private static final Logger logger = LoggerFactory.getLogger(NexmoSmsSender.class);

  private final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
  private final Meter smsMeter = metricRegistry.meter(name(getClass(), "sms", "delivered"));

  private static final String uri = "https://rest.nexmo.com/sms/json";

  private NexmoConfiguration nexmoConfiguration;
  private SmsVerificationConfiguration smsVerificationConfiguration;

  private final FaultTolerantHttpClient httpClient;

  public NexmoSmsSender(NexmoConfiguration nexmoConfiguration, SmsVerificationConfiguration smsVerificationConfiguration) {
    Executor executor = ExecutorUtils.newFixedThreadBoundedQueueExecutor(10, 100);

    this.nexmoConfiguration = nexmoConfiguration;
    this.smsVerificationConfiguration = smsVerificationConfiguration;

    this.httpClient = FaultTolerantHttpClient.newBuilder()
            .withCircuitBreaker(new CircuitBreakerConfiguration())
            .withRetry(new RetryConfiguration())
            .withVersion(HttpClient.Version.HTTP_2)
            .withConnectTimeout(Duration.ofSeconds(30))
            .withRedirect(HttpClient.Redirect.NEVER)
            .withExecutor(executor)
            .withName("nexmo")
            .build();
  }

  @Override
  public boolean isEnabled() {
    return nexmoConfiguration.isEnabled();
  }

  @Override
  public List<String> getPrefixes() {
    return Arrays.asList("+62877","+62878","+62879","+62817","+62818","+62819","+62859");
  }

  @Override
  public CompletableFuture<Boolean> deliverSmsVerification(String destination, Optional<String> clientType, String verificationCode) {
    String smsVerificationText;

    if ("ios".equals(clientType.orElse(null))) {
      smsVerificationText = smsVerificationConfiguration.getSmsIOSVerificationText();
    } else if ("android-ng".equals(clientType.orElse(null))) {
      smsVerificationText = smsVerificationConfiguration.getSmsAndroidNGVerificationText();
    } else {
      smsVerificationText = smsVerificationConfiguration.getSmsVerificationText();
    }

    Map<String, String> requestParameters = new HashMap<>();
    requestParameters.put("api_key", nexmoConfiguration.getApiKey());
    requestParameters.put("api_secret", nexmoConfiguration.getApiSecret());
    requestParameters.put("from", nexmoConfiguration.getBrandName());
    requestParameters.put("to", destination.replace("+", ""));
    requestParameters.put("text", String.format(smsVerificationText, verificationCode));

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(FormDataBodyPublisher.of(requestParameters))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

    smsMeter.mark();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(this::parseResponse);
  }

  @Override
  public CompletableFuture<Boolean> deliverVoxVerification(String destination, String verificationCode, Optional<String> locale) {
    return null;
  }

  private boolean parseResponse(HttpResponse<String> response) {
    logger.info("Nexmo sms response: " + response.statusCode() + response.body() != null ? ", " + response.body() : "");
    return true;
  }

}

package org.whispersystems.textsecuregcm.push;

import com.google.common.annotations.VisibleForTesting;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ApnMessage {

  public static final String APN_NOTIFICATION_PAYLOAD = "{\"aps\":{\"sound\":\"default\",\"alert\":{\"loc-key\":\"APN_Message\"}}}";
  public static final String APN_CHALLENGE_PAYLOAD    = "{\"aps\":{\"sound\":\"default\",\"alert\":{\"loc-key\":\"APN_Message\"}}, \"challenge\" : \"%s\"}";
  public static final String APN_COMMAND_PAYLOAD      = "{\"aps\":{\"sound\":\"default\",\"alert\":{\"loc-key\":\"APN_Message\"}}, \"command\" : \"%s\"}";
  public static final long   MAX_EXPIRATION           = Integer.MAX_VALUE * 1000L;

  private final String           apnId;
  private final String           number;
  private final long             deviceId;
  private final boolean          isVoip;
  private final Optional<String> data;
  private final boolean          isCommand;

  public ApnMessage(String apnId, String number, long deviceId, boolean isVoip, Optional<String> data) {
    this(apnId, number, deviceId, isVoip, data, false);
  }

  public ApnMessage(String apnId, String number, long deviceId, boolean isVoip, Optional<String> data, boolean isCommand) {
    this.apnId        = apnId;
    this.number       = number;
    this.deviceId     = deviceId;
    this.isVoip       = isVoip;
    this.data         = data;
    this.isCommand    = isCommand;
  }
  
  public boolean isVoip() {
    return isVoip;
  }

  public String getApnId() {
    return apnId;
  }

  public String getMessage() {
    if (!data.isPresent())          return APN_NOTIFICATION_PAYLOAD;
    else if (isCommand)             return String.format(APN_COMMAND_PAYLOAD, data.get());
    else                            return String.format(APN_CHALLENGE_PAYLOAD, data.get());
  }

  @VisibleForTesting
  public Optional<String> getData() {
    return data;
  }

  public long getExpirationTime() {
    return MAX_EXPIRATION;
  }

  public String getNumber() {
    return number;
  }

  public long getDeviceId() {
    return deviceId;
  }
}

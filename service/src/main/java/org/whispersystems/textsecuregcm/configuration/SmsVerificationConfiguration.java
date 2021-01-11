package org.whispersystems.textsecuregcm.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.whispersystems.textsecuregcm.sms.SmsSender;

public class SmsVerificationConfiguration {

  @JsonProperty
  @NotEmpty
  private String smsIOSVerificationText       = SmsSender.SMS_IOS_VERIFICATION_TEXT;

  @JsonProperty
  @NotEmpty
  private String smsAndroidNGVerificationText = SmsSender.SMS_ANDROID_NG_VERIFICATION_TEXT;

  @JsonProperty
  @NotEmpty
  private String smsVerificationText          = SmsSender.SMS_VERIFICATION_TEXT;

  public String getSmsIOSVerificationText() {
    return smsIOSVerificationText;
  }

  public String getSmsAndroidNGVerificationText() {
    return smsAndroidNGVerificationText;
  }

  public String getSmsVerificationText() {
    return smsVerificationText;
  }

}

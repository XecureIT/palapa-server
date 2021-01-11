package org.whispersystems.textsecuregcm.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ControlPanelConfiguration {

  @NotNull
  @JsonProperty
  private boolean enabled = false;

  @JsonProperty
  private String authKey;

  @JsonProperty
  private String authSecret;

  public boolean isEnabled() {
    return enabled;
  }

  public String getAuthKey() {
    return authKey;
  }

  public String getAuthSecret() {
    return authSecret;
  }

}

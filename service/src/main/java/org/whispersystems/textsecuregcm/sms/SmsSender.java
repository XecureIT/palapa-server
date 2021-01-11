/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.sms;


import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SmsSender {

  public static final String SMS_IOS_VERIFICATION_TEXT        = "Your SCP verification code: %s";
  public static final String SMS_ANDROID_NG_VERIFICATION_TEXT = "Your SCP verification code: %s";
  public static final String SMS_VERIFICATION_TEXT            = "Your SCP verification code: %s";

  private final List<ISmsSender> smsSender;

  public SmsSender(List<ISmsSender> smsSender) {
    this.smsSender = smsSender.stream().filter(s -> s.isEnabled()).collect(Collectors.toList());
  }

  private ISmsSender getSender(String destination) {
    if (smsSender.size() > 1) {
      for (ISmsSender sender : smsSender) {
        for (String prefix : sender.getPrefixes()) {
          if (destination.startsWith(prefix)) return sender;
        }
      }
    }
    return smsSender.get(0);
  }

  public void deliverSmsVerification(String destination, Optional<String> clientType, String verificationCode) {
    // Fix up mexico numbers to 'mobile' format just for SMS delivery.
    if (destination.startsWith("+52") && !destination.startsWith("+521")) {
      destination = "+521" + destination.substring(3);
    }

    getSender(destination).deliverSmsVerification(destination, clientType, verificationCode);
  }

  public void deliverVoxVerification(String destination, String verificationCode, Optional<String> locale) {
    getSender(destination).deliverVoxVerification(destination, verificationCode, locale);
  }
}

/*
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
package org.whispersystems.textsecuregcm.controllers;

import com.amazonaws.HttpMethod;
import com.codahale.metrics.annotation.Timed;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.entities.AttachmentDescriptorV1;
import org.whispersystems.textsecuregcm.entities.AttachmentUri;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.s3.UrlSigner;
import org.whispersystems.textsecuregcm.storage.Account;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.dropwizard.auth.Auth;

import java.net.URL;
import java.util.stream.Stream;


@Path("/v1/attachments")
public class AttachmentControllerV1 extends AttachmentControllerBase {

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(AttachmentControllerV1.class);

  private static final String[] UNACCELERATED_REGIONS = {"+20", "+971", "+968", "+974"};

  private final RateLimiters rateLimiters;
  private final UrlSigner    urlSigner;
  private final MinioClient  minioClient;
  private final String       bucket;

  public AttachmentControllerV1(RateLimiters rateLimiters, String accessKey, String accessSecret, String bucket, String endpoint) throws Exception {
    this.rateLimiters = rateLimiters;
    this.urlSigner    = new UrlSigner(accessKey, accessSecret, bucket);
    this.minioClient  = new MinioClient(endpoint, accessKey, accessSecret);
    this.bucket       = bucket;
  }

  @Timed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public AttachmentDescriptorV1 allocateAttachment(@Auth Account account)
      throws Exception
  {
    if (account.isRateLimited()) {
      rateLimiters.getAttachmentLimiter().validate(account.getNumber());
    }

    long attachmentId = generateAttachmentId();
//    URL  url          = urlSigner.getPreSignedUrl(attachmentId, HttpMethod.PUT, Stream.of(UNACCELERATED_REGIONS).anyMatch(region -> account.getNumber().startsWith(region)));
    URL  url          = new URL(minioClient.presignedPutObject(bucket, String.valueOf(attachmentId), 60 * 60 * 1));

    return new AttachmentDescriptorV1(attachmentId, url.toExternalForm());
  }

  @Timed
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{attachmentId}")
  public AttachmentUri redirectToAttachment(@Auth                      Account account,
                                            @PathParam("attachmentId") long    attachmentId)
      throws Exception
  {
//    return new AttachmentUri(urlSigner.getPreSignedUrl(attachmentId, HttpMethod.GET, Stream.of(UNACCELERATED_REGIONS).anyMatch(region -> account.getNumber().startsWith(region))));
    return new AttachmentUri(minioClient.presignedGetObject(bucket, String.valueOf(attachmentId), 60 * 60 * 1));
  }

}

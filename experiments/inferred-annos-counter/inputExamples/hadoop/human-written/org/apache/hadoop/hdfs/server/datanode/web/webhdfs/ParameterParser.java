/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.server.datanode.web.webhdfs;

import static org.apache.hadoop.hdfs.protocol.HdfsConstants.HDFS_URI_SCHEME;
import static org.apache.hadoop.hdfs.server.datanode.web.webhdfs.WebHdfsHandler.WEBHDFS_PREFIX_LENGTH;

import io.netty.handler.codec.http.QueryStringDecoder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.HAUtilClient;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.web.resources.BlockSizeParam;
import org.apache.hadoop.hdfs.web.resources.BufferSizeParam;
import org.apache.hadoop.hdfs.web.resources.CreateFlagParam;
import org.apache.hadoop.hdfs.web.resources.CreateParentParam;
import org.apache.hadoop.hdfs.web.resources.DelegationParam;
import org.apache.hadoop.hdfs.web.resources.DoAsParam;
import org.apache.hadoop.hdfs.web.resources.HttpOpParam;
import org.apache.hadoop.hdfs.web.resources.LengthParam;
import org.apache.hadoop.hdfs.web.resources.NamenodeAddressParam;
import org.apache.hadoop.hdfs.web.resources.NoRedirectParam;
import org.apache.hadoop.hdfs.web.resources.OffsetParam;
import org.apache.hadoop.hdfs.web.resources.OverwriteParam;
import org.apache.hadoop.hdfs.web.resources.PermissionParam;
import org.apache.hadoop.hdfs.web.resources.ReplicationParam;
import org.apache.hadoop.hdfs.web.resources.UnmaskedPermissionParam;
import org.apache.hadoop.hdfs.web.resources.UserParam;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.token.Token;

class ParameterParser {
  private final Configuration conf;
  private final String path;
  private final Map<String, List<String>> params;

  ParameterParser(QueryStringDecoder decoder, Configuration conf) {
    this.path = decoder.path().substring(WEBHDFS_PREFIX_LENGTH);
    this.params = decoder.parameters();
    this.conf = conf;
  }

  String path() {
    return path;
  }

  String op() {
    return param(HttpOpParam.NAME);
  }

  long offset() {
    return new OffsetParam(param(OffsetParam.NAME)).getOffset();
  }

  long length() {
    return new LengthParam(param(LengthParam.NAME)).getLength();
  }

  String namenodeId() {
    return new NamenodeAddressParam(param(NamenodeAddressParam.NAME)).getValue();
  }

  String doAsUser() {
    return new DoAsParam(param(DoAsParam.NAME)).getValue();
  }

  String userName() {
    return new UserParam(param(UserParam.NAME)).getValue();
  }

  int bufferSize() {
    return new BufferSizeParam(param(BufferSizeParam.NAME)).getValue(conf);
  }

  long blockSize() {
    return new BlockSizeParam(param(BlockSizeParam.NAME)).getValue(conf);
  }

  short replication() {
    return new ReplicationParam(param(ReplicationParam.NAME)).getValue(conf);
  }

  FsPermission permission() {
    return new PermissionParam(param(PermissionParam.NAME)).getFileFsPermission();
  }

  FsPermission unmaskedPermission() {
    String value = param(UnmaskedPermissionParam.NAME);
    return value == null ? null : new UnmaskedPermissionParam(value).getFileFsPermission();
  }

  boolean overwrite() {
    return new OverwriteParam(param(OverwriteParam.NAME)).getValue();
  }

  boolean noredirect() {
    return new NoRedirectParam(param(NoRedirectParam.NAME)).getValue();
  }

  Token<DelegationTokenIdentifier> delegationToken() throws IOException {
    String delegation = param(DelegationParam.NAME);
    final Token<DelegationTokenIdentifier> token = new Token<DelegationTokenIdentifier>();
    token.decodeFromUrlString(delegation);
    URI nnUri = URI.create(HDFS_URI_SCHEME + "://" + namenodeId());
    boolean isLogical = HAUtilClient.isLogicalUri(conf, nnUri);
    if (isLogical) {
      token.setService(HAUtilClient.buildTokenServiceForLogicalUri(nnUri, HDFS_URI_SCHEME));
    } else {
      token.setService(SecurityUtil.buildTokenService(nnUri));
    }
    return token;
  }

  public boolean createParent() {
    return new CreateParentParam(param(CreateParentParam.NAME)).getValue();
  }

  public EnumSet<CreateFlag> createFlag() {
    String cf = "";
    if (param(CreateFlagParam.NAME) != null) {
      QueryStringDecoder decoder =
          new QueryStringDecoder(param(CreateFlagParam.NAME), StandardCharsets.UTF_8);
      cf = decoder.path();
    }
    return new CreateFlagParam(cf).getValue();
  }

  Configuration conf() {
    return conf;
  }

  private String param(String key) {
    List<String> p = params.get(key);
    return p == null ? null : p.get(0);
  }

  /**
   * Helper to decode half of a hexadecimal number from a string.
   *
   * @param c The ASCII character of the hexadecimal number to decode. Must be in the range {@code
   *     [0-9a-fA-F]}.
   * @return The hexadecimal value represented in the ASCII character given, or {@link
   *     Character#MAX_VALUE} if the character is invalid.
   */
  private static char decodeHexNibble(final char c) {
    if ('0' <= c && c <= '9') {
      return (char) (c - '0');
    } else if ('a' <= c && c <= 'f') {
      return (char) (c - 'a' + 10);
    } else if ('A' <= c && c <= 'F') {
      return (char) (c - 'A' + 10);
    } else {
      return Character.MAX_VALUE;
    }
  }
}

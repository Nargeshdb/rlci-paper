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
package org.apache.hadoop.hdfs;

import org.apache.hadoop.hdfs.web.WebHdfsConstants;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DtFetcher for WebHdfsFileSystem using the base class HdfsDtFetcher impl. */
public class WebHdfsDtFetcher extends HdfsDtFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(WebHdfsDtFetcher.class);

  private static final String SERVICE_NAME = WebHdfsConstants.WEBHDFS_SCHEME;

  @Override
  public Text getServiceName() {
    return new Text(SERVICE_NAME);
  }
}

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
package org.apache.hadoop.hdfs.server.namenode.sps;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.protocol.Block;

/** Interface for notifying about block movement attempt completion. */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface BlockMovementListener {

  /**
   * This method used to notify to the SPS about block movement attempt finished. Then SPS will
   * re-check whether it needs retry or not.
   *
   * @param moveAttemptFinishedBlks -list of movement attempt finished blocks
   */
  void notifyMovementTriedBlocks(Block[] moveAttemptFinishedBlks);
}

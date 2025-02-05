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
package org.apache.hadoop.hdfs.server.blockmanagement;

import java.util.*;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.fs.StorageType;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.net.Node;
import org.apache.hadoop.net.NodeBase;

/**
 * The class is responsible for choosing the desired number of targets for placing block replicas.
 * The strategy is that it tries its best to place the replicas to most racks.
 */
@InterfaceAudience.Private
public class BlockPlacementPolicyRackFaultTolerant extends BlockPlacementPolicyDefault {

  @Override
  protected int[] getMaxNodesPerRack(int numOfChosen, int numOfReplicas) {
    int clusterSize = clusterMap.getNumOfLeaves();
    int totalNumOfReplicas = numOfChosen + numOfReplicas;
    if (totalNumOfReplicas > clusterSize) {
      numOfReplicas -= (totalNumOfReplicas - clusterSize);
      totalNumOfReplicas = clusterSize;
    }
    // No calculation needed when there is only one rack or picking one node.
    int numOfRacks = clusterMap.getNumOfRacks();
    // HDFS-14527 return default when numOfRacks = 0 to avoid
    // ArithmeticException when calc maxNodesPerRack at following logic.
    if (numOfRacks <= 1 || totalNumOfReplicas <= 1) {
      return new int[] {numOfReplicas, totalNumOfReplicas};
    }
    // If more racks than replicas, put one replica per rack.
    if (totalNumOfReplicas < numOfRacks) {
      return new int[] {numOfReplicas, 1};
    }
    // If more replicas than racks, evenly spread the replicas.
    // This calculation rounds up.
    int maxNodesPerRack = (totalNumOfReplicas - 1) / numOfRacks + 1;
    return new int[] {numOfReplicas, maxNodesPerRack};
  }

  /**
   * Choose numOfReplicas in order: 1. If total replica expected is less than numOfRacks in cluster,
   * it choose randomly. 2. If total replica expected is bigger than numOfRacks, it choose: 2a. Fill
   * each rack exactly (maxNodesPerRack-1) replicas. 2b. For some random racks, place one more
   * replica to each one of them, until numOfReplicas have been chosen. <br>
   * 3. If after step 2, there are still replicas not placed (due to some racks have fewer datanodes
   * than maxNodesPerRack), the rest of the replicas is placed evenly on the rest of the racks who
   * have Datanodes that have not been placed a replica. 4. If after step 3, there are still
   * replicas not placed. A {@link NotEnoughReplicasException} is thrown.
   *
   * <p>For normal setups, step 2 would suffice. So in the end, the difference of the numbers of
   * replicas for each two racks is no more than 1. Either way it always prefer local storage.
   *
   * @return local node of writer
   */
  @Override
  protected Node chooseTargetInOrder(
      int numOfReplicas,
      Node writer,
      final Set<Node> excludedNodes,
      final long blocksize,
      final int maxNodesPerRack,
      final List<DatanodeStorageInfo> results,
      final boolean avoidStaleNodes,
      final boolean newBlock,
      EnumMap<StorageType, Integer> storageTypes)
      throws NotEnoughReplicasException {
    int totalReplicaExpected = results.size() + numOfReplicas;
    int numOfRacks = clusterMap.getNumOfRacks();
    if (totalReplicaExpected < numOfRacks || totalReplicaExpected % numOfRacks == 0) {
      writer =
          chooseOnce(
              numOfReplicas,
              writer,
              excludedNodes,
              blocksize,
              maxNodesPerRack,
              results,
              avoidStaleNodes,
              storageTypes);
      return writer;
    }

    assert totalReplicaExpected > (maxNodesPerRack - 1) * numOfRacks;

    // Calculate numOfReplicas for filling each rack exactly (maxNodesPerRack-1)
    // replicas.
    HashMap<String, Integer> rackCounts = new HashMap<>();
    for (DatanodeStorageInfo dsInfo : results) {
      String rack = dsInfo.getDatanodeDescriptor().getNetworkLocation();
      Integer count = rackCounts.get(rack);
      if (count != null) {
        rackCounts.put(rack, count + 1);
      } else {
        rackCounts.put(rack, 1);
      }
    }
    int excess = 0; // Sum of the above (maxNodesPerRack-1) part of nodes in results
    for (int count : rackCounts.values()) {
      if (count > maxNodesPerRack - 1) {
        excess += count - (maxNodesPerRack - 1);
      }
    }
    numOfReplicas =
        Math.min(
            totalReplicaExpected - results.size(),
            (maxNodesPerRack - 1) * numOfRacks - (results.size() - excess));

    try {
      // Try to spread the replicas as evenly as possible across racks.
      // This is done by first placing with (maxNodesPerRack-1), then spreading
      // the remainder by calling again with maxNodesPerRack.
      writer =
          chooseOnce(
              numOfReplicas,
              writer,
              new HashSet<>(excludedNodes),
              blocksize,
              maxNodesPerRack - 1,
              results,
              avoidStaleNodes,
              storageTypes);

      // Exclude the chosen nodes
      for (DatanodeStorageInfo resultStorage : results) {
        addToExcludedNodes(resultStorage.getDatanodeDescriptor(), excludedNodes);
      }
      LOG.trace("Chosen nodes: {}", results);
      LOG.trace("Excluded nodes: {}", excludedNodes);

      numOfReplicas = totalReplicaExpected - results.size();
      chooseOnce(
          numOfReplicas,
          writer,
          excludedNodes,
          blocksize,
          maxNodesPerRack,
          results,
          avoidStaleNodes,
          storageTypes);
    } catch (NotEnoughReplicasException e) {
      LOG.warn(
          "Only able to place {} of total expected {}"
              + " (maxNodesPerRack={}, numOfReplicas={}) nodes "
              + "evenly across racks, falling back to evenly place on the "
              + "remaining racks. This may not guarantee rack-level fault "
              + "tolerance. Please check if the racks are configured properly.",
          results.size(),
          totalReplicaExpected,
          maxNodesPerRack,
          numOfReplicas);
      LOG.debug("Caught exception was:", e);
      chooseEvenlyFromRemainingRacks(
          writer,
          excludedNodes,
          blocksize,
          maxNodesPerRack,
          results,
          avoidStaleNodes,
          storageTypes,
          totalReplicaExpected,
          e);
    }

    return writer;
  }

  /** Choose as evenly as possible from the racks which have available datanodes. */
  private void chooseEvenlyFromRemainingRacks(
      Node writer,
      Set<Node> excludedNodes,
      long blocksize,
      int maxNodesPerRack,
      List<DatanodeStorageInfo> results,
      boolean avoidStaleNodes,
      EnumMap<StorageType, Integer> storageTypes,
      int totalReplicaExpected,
      NotEnoughReplicasException e)
      throws NotEnoughReplicasException {
    int numResultsOflastChoose = 0;
    NotEnoughReplicasException lastException = e;
    int bestEffortMaxNodesPerRack = maxNodesPerRack;
    while (results.size() != totalReplicaExpected && numResultsOflastChoose != results.size()) {
      // Exclude the chosen nodes
      final Set<Node> newExcludeNodes = new HashSet<>();
      for (DatanodeStorageInfo resultStorage : results) {
        addToExcludedNodes(resultStorage.getDatanodeDescriptor(), newExcludeNodes);
      }

      LOG.trace("Chosen nodes: {}", results);
      LOG.trace("Excluded nodes: {}", excludedNodes);
      LOG.trace("New Excluded nodes: {}", newExcludeNodes);
      final int numOfReplicas = totalReplicaExpected - results.size();
      numResultsOflastChoose = results.size();
      try {
        chooseOnce(
            numOfReplicas,
            writer,
            newExcludeNodes,
            blocksize,
            ++bestEffortMaxNodesPerRack,
            results,
            avoidStaleNodes,
            storageTypes);
      } catch (NotEnoughReplicasException nere) {
        lastException = nere;
      } finally {
        excludedNodes.addAll(newExcludeNodes);
      }
    }

    if (numResultsOflastChoose != totalReplicaExpected) {
      LOG.debug(
          "Best effort placement failed: expecting {} replicas, only " + "chose {}.",
          totalReplicaExpected,
          numResultsOflastChoose);
      throw lastException;
    }
  }

  /**
   * Randomly choose <i>numOfReplicas</i> targets from the given <i>scope</i>. Except that 1st
   * replica prefer local storage.
   *
   * @return local node of writer.
   */
  private Node chooseOnce(
      int numOfReplicas,
      Node writer,
      final Set<Node> excludedNodes,
      final long blocksize,
      final int maxNodesPerRack,
      final List<DatanodeStorageInfo> results,
      final boolean avoidStaleNodes,
      EnumMap<StorageType, Integer> storageTypes)
      throws NotEnoughReplicasException {
    if (numOfReplicas == 0) {
      return writer;
    }
    writer =
        chooseLocalStorage(
                writer,
                excludedNodes,
                blocksize,
                maxNodesPerRack,
                results,
                avoidStaleNodes,
                storageTypes,
                true)
            .getDatanodeDescriptor();
    if (--numOfReplicas == 0) {
      return writer;
    }
    chooseRandom(
        numOfReplicas,
        NodeBase.ROOT,
        excludedNodes,
        blocksize,
        maxNodesPerRack,
        results,
        avoidStaleNodes,
        storageTypes);
    return writer;
  }

  @Override
  public BlockPlacementStatus verifyBlockPlacement(DatanodeInfo[] locs, int numberOfReplicas) {
    if (locs == null) locs = DatanodeDescriptor.EMPTY_ARRAY;
    if (!clusterMap.hasClusterEverBeenMultiRack()) {
      // only one rack
      return new BlockPlacementStatusDefault(1, 1, 1);
    }
    // 1. Check that all locations are different.
    // 2. Count locations on different racks.
    Set<String> racks = new TreeSet<>();
    for (DatanodeInfo dn : locs) {
      racks.add(dn.getNetworkLocation());
    }
    return new BlockPlacementStatusDefault(
        racks.size(), numberOfReplicas, clusterMap.getNumOfRacks());
  }

  @Override
  protected Collection<DatanodeStorageInfo> pickupReplicaSet(
      Collection<DatanodeStorageInfo> moreThanOne,
      Collection<DatanodeStorageInfo> exactlyOne,
      Map<String, List<DatanodeStorageInfo>> rackMap) {
    return moreThanOne.isEmpty() ? exactlyOne : moreThanOne;
  }
}

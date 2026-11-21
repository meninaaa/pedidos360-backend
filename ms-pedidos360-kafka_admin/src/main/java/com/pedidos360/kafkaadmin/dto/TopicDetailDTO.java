package com.pedidos360.kafkaadmin.dto;

import java.util.List;
import java.util.Map;

public class TopicDetailDTO {
    private String name;
    private int partitionsCount;
    private int replicationFactor;
    private List<PartitionInfoDTO> partitions;
    private Map<String, String> relevantConfigs;

    public TopicDetailDTO() {}

    public TopicDetailDTO(String name, int partitionsCount, int replicationFactor,
                          List<PartitionInfoDTO> partitions, Map<String, String> relevantConfigs) {
        this.name = name;
        this.partitionsCount = partitionsCount;
        this.replicationFactor = replicationFactor;
        this.partitions = partitions;
        this.relevantConfigs = relevantConfigs;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPartitionsCount() { return partitionsCount; }
    public void setPartitionsCount(int partitionsCount) { this.partitionsCount = partitionsCount; }

    public int getReplicationFactor() { return replicationFactor; }
    public void setReplicationFactor(int replicationFactor) { this.replicationFactor = replicationFactor; }

    public List<PartitionInfoDTO> getPartitions() { return partitions; }
    public void setPartitions(List<PartitionInfoDTO> partitions) { this.partitions = partitions; }

    public Map<String, String> getRelevantConfigs() { return relevantConfigs; }
    public void setRelevantConfigs(Map<String, String> relevantConfigs) { this.relevantConfigs = relevantConfigs; }

    public static class PartitionInfoDTO {
        private int partition;
        private int leader;
        private List<Integer> replicas;
        private List<Integer> isr;

        public PartitionInfoDTO() {}

        public PartitionInfoDTO(int partition, int leader, List<Integer> replicas, List<Integer> isr) {
            this.partition = partition;
            this.leader = leader;
            this.replicas = replicas;
            this.isr = isr;
        }

        public int getPartition() { return partition; }
        public void setPartition(int partition) { this.partition = partition; }

        public int getLeader() { return leader; }
        public void setLeader(int leader) { this.leader = leader; }

        public List<Integer> getReplicas() { return replicas; }
        public void setReplicas(List<Integer> replicas) { this.replicas = replicas; }

        public List<Integer> getIsr() { return isr; }
        public void setIsr(List<Integer> isr) { this.isr = isr; }
    }
}
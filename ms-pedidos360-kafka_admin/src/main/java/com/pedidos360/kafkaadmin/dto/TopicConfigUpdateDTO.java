package com.pedidos360.kafkaadmin.dto;

public class TopicConfigUpdateDTO {
    private Integer newPartitionCount;
    private Long retentionMs;
    private Long retentionBytes;
    private String cleanupPolicy;
    private Integer minInsyncReplicas;

    public TopicConfigUpdateDTO() {}

    public Integer getNewPartitionCount() { return newPartitionCount; }
    public void setNewPartitionCount(Integer newPartitionCount) { this.newPartitionCount = newPartitionCount; }

    public Long getRetentionMs() { return retentionMs; }
    public void setRetentionMs(Long retentionMs) { this.retentionMs = retentionMs; }

    public Long getRetentionBytes() { return retentionBytes; }
    public void setRetentionBytes(Long retentionBytes) { this.retentionBytes = retentionBytes; }

    public String getCleanupPolicy() { return cleanupPolicy; }
    public void setCleanupPolicy(String cleanupPolicy) { this.cleanupPolicy = cleanupPolicy; }

    public Integer getMinInsyncReplicas() { return minInsyncReplicas; }
    public void setMinInsyncReplicas(Integer minInsyncReplicas) { this.minInsyncReplicas = minInsyncReplicas; }
}
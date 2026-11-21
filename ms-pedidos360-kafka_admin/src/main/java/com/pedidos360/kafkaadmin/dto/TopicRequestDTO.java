package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class TopicRequestDTO {

    @NotBlank(message = "El nombre del tópico es obligatorio")
    private String name;

    @Min(value = 1, message = "Debe tener al menos 1 partición")
    private int partitions = 3;

    @Min(value = 1, message = "El factor de replicación debe ser al menos 1")
    private short replicationFactor = 3;

    private Long retentionMs = 604800000L;
    private String cleanupPolicy = "delete";
    private Integer minInsyncReplicas = 2;

    public TopicRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPartitions() { return partitions; }
    public void setPartitions(int partitions) { this.partitions = partitions; }

    public short getReplicationFactor() { return replicationFactor; }
    public void setReplicationFactor(short replicationFactor) { this.replicationFactor = replicationFactor; }

    public Long getRetentionMs() { return retentionMs; }
    public void setRetentionMs(Long retentionMs) { this.retentionMs = retentionMs; }

    public String getCleanupPolicy() { return cleanupPolicy; }
    public void setCleanupPolicy(String cleanupPolicy) { this.cleanupPolicy = cleanupPolicy; }

    public Integer getMinInsyncReplicas() { return minInsyncReplicas; }
    public void setMinInsyncReplicas(Integer minInsyncReplicas) { this.minInsyncReplicas = minInsyncReplicas; }
}
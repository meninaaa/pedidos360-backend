package com.pedidos360.kafkaadmin.dto;

import java.util.List;

public class ConsumerGroupDetailDTO {
    private String groupId;
    private String state;
    private int membersCount;
    private long totalLag;
    private List<PartitionOffsetDTO> partitionOffsets;

    public ConsumerGroupDetailDTO() {}

    public ConsumerGroupDetailDTO(String groupId, String state, int membersCount, long totalLag,
                                  List<PartitionOffsetDTO> partitionOffsets) {
        this.groupId = groupId;
        this.state = state;
        this.membersCount = membersCount;
        this.totalLag = totalLag;
        this.partitionOffsets = partitionOffsets;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getMembersCount() { return membersCount; }
    public void setMembersCount(int membersCount) { this.membersCount = membersCount; }

    public long getTotalLag() { return totalLag; }
    public void setTotalLag(long totalLag) { this.totalLag = totalLag; }

    public List<PartitionOffsetDTO> getPartitionOffsets() { return partitionOffsets; }
    public void setPartitionOffsets(List<PartitionOffsetDTO> partitionOffsets) { this.partitionOffsets = partitionOffsets; }

    public static class PartitionOffsetDTO {
        private String topic;
        private int partition;
        private long currentOffset;
        private long logEndOffset;
        private long lag;

        public PartitionOffsetDTO() {}

        public PartitionOffsetDTO(String topic, int partition, long currentOffset, long logEndOffset, long lag) {
            this.topic = topic;
            this.partition = partition;
            this.currentOffset = currentOffset;
            this.logEndOffset = logEndOffset;
            this.lag = lag;
        }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public int getPartition() { return partition; }
        public void setPartition(int partition) { this.partition = partition; }

        public long getCurrentOffset() { return currentOffset; }
        public void setCurrentOffset(long currentOffset) { this.currentOffset = currentOffset; }

        public long getLogEndOffset() { return logEndOffset; }
        public void setLogEndOffset(long logEndOffset) { this.logEndOffset = logEndOffset; }

        public long getLag() { return lag; }
        public void setLag(long lag) { this.lag = lag; }
    }
}
package com.pedidos360.kafkaadmin.service;

import com.pedidos360.kafkaadmin.dto.ConsumerGroupDetailDTO;
import com.pedidos360.kafkaadmin.dto.TopicConfigUpdateDTO;
import com.pedidos360.kafkaadmin.dto.TopicDetailDTO;
import com.pedidos360.kafkaadmin.dto.TopicRequestDTO;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class KafkaAdminService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdminService.class);
    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public TopicDetailDTO createTopic(TopicRequestDTO request) throws ExecutionException, InterruptedException {
        Map<String, String> configs = new HashMap<>();
        if (request.getRetentionMs() != null) {
            configs.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(request.getRetentionMs()));
        }
        if (request.getCleanupPolicy() != null && !request.getCleanupPolicy().isBlank()) {
            configs.put(TopicConfig.CLEANUP_POLICY_CONFIG, request.getCleanupPolicy());
        }
        if (request.getMinInsyncReplicas() != null) {
            configs.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(request.getMinInsyncReplicas()));
        }

        NewTopic newTopic = new NewTopic(request.getName(), request.getPartitions(), request.getReplicationFactor())
                .configs(configs);

        adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        log.info("Tópico '{}' creado con {} particiones y {} réplicas",
                request.getName(), request.getPartitions(), request.getReplicationFactor());

        return getTopicDetail(request.getName());
    }

    public List<TopicDetailDTO> listAllTopics() throws ExecutionException, InterruptedException {
        Set<String> topicNames = adminClient.listTopics().names().get();
        List<TopicDetailDTO> result = new ArrayList<>();
        for (String name : topicNames) {
            if (!name.startsWith("__")) {
                result.add(getTopicDetail(name));
            }
        }
        return result;
    }

    public TopicDetailDTO getTopicDetail(String topicName) throws ExecutionException, InterruptedException {
        TopicDescription description = adminClient.describeTopics(Collections.singletonList(topicName))
                .allTopicNames().get().get(topicName);

        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
        Config config = adminClient.describeConfigs(Collections.singletonList(resource)).all().get().get(resource);

        Map<String, String> relevantConfigs = new LinkedHashMap<>();
        List<String> keysToExtract = List.of(
                TopicConfig.RETENTION_MS_CONFIG,
                TopicConfig.RETENTION_BYTES_CONFIG,
                TopicConfig.CLEANUP_POLICY_CONFIG,
                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG
        );
        for (String key : keysToExtract) {
            ConfigEntry entry = config.get(key);
            if (entry != null) {
                relevantConfigs.put(key, entry.value());
            }
        }

        List<TopicDetailDTO.PartitionInfoDTO> partitions = description.partitions().stream()
                .map(p -> new TopicDetailDTO.PartitionInfoDTO(
                        p.partition(),
                        p.leader() != null ? p.leader().id() : -1,
                        p.replicas().stream().map(Node::id).toList(),
                        p.isr().stream().map(Node::id).toList()
                ))
                .toList();

        int replicationFactor = partitions.isEmpty() ? 0 : partitions.get(0).getReplicas().size();

        return new TopicDetailDTO(
                description.name(),
                partitions.size(),
                replicationFactor,
                partitions,
                relevantConfigs
        );
    }

    public TopicDetailDTO updateTopic(String topicName, TopicConfigUpdateDTO updateDTO)
            throws ExecutionException, InterruptedException {

        if (updateDTO.getNewPartitionCount() != null) {
            Map<String, NewPartitions> partitionsMap = Map.of(
                    topicName, NewPartitions.increaseTo(updateDTO.getNewPartitionCount())
            );
            adminClient.createPartitions(partitionsMap).all().get();
            log.info("Particiones de '{}' incrementadas a {}", topicName, updateDTO.getNewPartitionCount());
        }

        Collection<AlterConfigOp> ops = new ArrayList<>();
        if (updateDTO.getRetentionMs() != null) {
            ops.add(new AlterConfigOp(
                    new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(updateDTO.getRetentionMs())),
                    AlterConfigOp.OpType.SET));
        }
        if (updateDTO.getRetentionBytes() != null) {
            ops.add(new AlterConfigOp(
                    new ConfigEntry(TopicConfig.RETENTION_BYTES_CONFIG, String.valueOf(updateDTO.getRetentionBytes())),
                    AlterConfigOp.OpType.SET));
        }
        if (updateDTO.getCleanupPolicy() != null && !updateDTO.getCleanupPolicy().isBlank()) {
            ops.add(new AlterConfigOp(
                    new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG, updateDTO.getCleanupPolicy()),
                    AlterConfigOp.OpType.SET));
        }
        if (updateDTO.getMinInsyncReplicas() != null) {
            ops.add(new AlterConfigOp(
                    new ConfigEntry(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(updateDTO.getMinInsyncReplicas())),
                    AlterConfigOp.OpType.SET));
        }

        if (!ops.isEmpty()) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            adminClient.incrementalAlterConfigs(Map.of(resource, ops)).all().get();
            log.info("Configuraciones de '{}' actualizadas correctamente", topicName);
        }

        return getTopicDetail(topicName);
    }

    public void deleteTopic(String topicName) throws ExecutionException, InterruptedException {
        adminClient.deleteTopics(Collections.singletonList(topicName)).all().get();
        log.info("Tópico '{}' eliminado", topicName);
    }

    public List<String> listConsumerGroups() throws ExecutionException, InterruptedException {
        return adminClient.listConsumerGroups().all().get().stream()
                .map(ConsumerGroupListing::groupId)
                .sorted()
                .toList();
    }

    public ConsumerGroupDetailDTO inspectConsumerGroup(String groupId)
            throws ExecutionException, InterruptedException {

        ConsumerGroupDescription groupDesc = adminClient.describeConsumerGroups(Collections.singletonList(groupId))
                .all().get().get(groupId);

        Map<TopicPartition, OffsetAndMetadata> committedOffsets =
                adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, OffsetSpec> latestSpecs = committedOffsets.keySet().stream()
                .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));

        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets =
                latestSpecs.isEmpty() ? Collections.emptyMap() : adminClient.listOffsets(latestSpecs).all().get();

        List<ConsumerGroupDetailDTO.PartitionOffsetDTO> partitionOffsets = new ArrayList<>();
        long totalLag = 0;

        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : committedOffsets.entrySet()) {
            TopicPartition tp = entry.getKey();
            long currentOffset = entry.getValue() != null ? entry.getValue().offset() : 0L;
            long endOffset = endOffsets.containsKey(tp) ? endOffsets.get(tp).offset() : currentOffset;
            long lag = Math.max(0L, endOffset - currentOffset);
            totalLag += lag;

            partitionOffsets.add(new ConsumerGroupDetailDTO.PartitionOffsetDTO(
                    tp.topic(),
                    tp.partition(),
                    currentOffset,
                    endOffset,
                    lag
            ));
        }

        partitionOffsets.sort(Comparator.comparing(ConsumerGroupDetailDTO.PartitionOffsetDTO::getTopic)
                .thenComparingInt(ConsumerGroupDetailDTO.PartitionOffsetDTO::getPartition));

        return new ConsumerGroupDetailDTO(
                groupId,
                groupDesc.state().toString(),
                groupDesc.members().size(),
                totalLag,
                partitionOffsets
        );
    }
}
package com.pedidos360.kafkaadmin.controller;

import com.pedidos360.kafkaadmin.dto.ConsumerGroupDetailDTO;
import com.pedidos360.kafkaadmin.dto.TopicConfigUpdateDTO;
import com.pedidos360.kafkaadmin.dto.TopicDetailDTO;
import com.pedidos360.kafkaadmin.dto.TopicRequestDTO;
import com.pedidos360.kafkaadmin.service.KafkaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @PostMapping("/topics")
    public ResponseEntity<TopicDetailDTO> createTopic(@Valid @RequestBody TopicRequestDTO request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(kafkaAdminService.createTopic(request));
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicDetailDTO>> listTopics()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminService.listAllTopics());
    }

    @GetMapping("/topics/{topicName}")
    public ResponseEntity<TopicDetailDTO> getTopic(@PathVariable String topicName)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminService.getTopicDetail(topicName));
    }

    @PatchMapping("/topics/{topicName}")
    public ResponseEntity<TopicDetailDTO> updateTopic(
            @PathVariable String topicName,
            @RequestBody TopicConfigUpdateDTO updateDTO)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminService.updateTopic(topicName, updateDTO));
    }

    @DeleteMapping("/topics/{topicName}")
    public ResponseEntity<Object> deleteTopic(@PathVariable String topicName)
            throws ExecutionException, InterruptedException {
        kafkaAdminService.deleteTopic(topicName);
        return ResponseEntity.ok(Map.of("message", "Tópico '" + topicName + "' eliminado correctamente"));
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<List<String>> listConsumerGroups()
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminService.listConsumerGroups());
    }

    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<ConsumerGroupDetailDTO> inspectConsumerGroup(@PathVariable String groupId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminService.inspectConsumerGroup(groupId));
    }
}
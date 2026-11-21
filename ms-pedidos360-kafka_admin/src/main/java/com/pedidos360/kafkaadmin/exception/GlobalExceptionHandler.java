package com.pedidos360.kafkaadmin.exception;

import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Object> handleKafkaExecutionException(ExecutionException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof TopicExistsException) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El tópico ya existe en el clúster Kafka", "detail", cause.getMessage()));
        }
        if (cause instanceof UnknownTopicOrPartitionException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tópico o partición no encontrada", "detail", cause.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error ejecutando operación en Kafka AdminClient",
                        "detail", cause != null ? cause.getMessage() : ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Datos de entrada inválidos");
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }
}
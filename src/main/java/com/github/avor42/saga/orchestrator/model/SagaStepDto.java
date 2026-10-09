package com.github.avor42.saga.orchestrator.model;

import java.util.UUID;

public record SagaStepDto<T>(UUID uuid, String step, String replyTo, String correlationId, T context) {
}
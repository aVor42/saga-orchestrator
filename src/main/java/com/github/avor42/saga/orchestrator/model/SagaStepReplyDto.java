package com.github.avor42.saga.orchestrator.model;

import java.util.UUID;

public record SagaStepReplyDto<T>(UUID uuid, String step,  String correlationId, T context) {
}

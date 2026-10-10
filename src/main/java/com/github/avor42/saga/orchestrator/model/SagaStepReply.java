package com.github.avor42.saga.orchestrator.model;

import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionStatus;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;

import java.util.UUID;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public record SagaStepReply<E, S>(
        E saga,
        S step,
        SagaStepActionType type,
        SagaStepActionStatus status
) {

}

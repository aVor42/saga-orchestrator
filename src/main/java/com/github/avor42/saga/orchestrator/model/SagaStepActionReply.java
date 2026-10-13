package com.github.avor42.saga.orchestrator.model;

import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionStatus;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public record SagaStepActionReply<ID, S>(
        ID sagaId,
        String sagaName,
        S step,
        SagaStepActionType type,
        SagaStepActionStatus status
) {

}

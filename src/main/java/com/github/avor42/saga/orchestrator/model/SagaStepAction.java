package com.github.avor42.saga.orchestrator.model;

import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;

/**
 * @author Voronkov A
 * @since 11.10.2026
 */
public record SagaStepAction<E, S>(
        E saga,
        S step,
        SagaStepActionType type
) {

}

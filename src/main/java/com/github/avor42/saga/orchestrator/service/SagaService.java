package com.github.avor42.saga.orchestrator.service;

import com.github.avor42.saga.orchestrator.model.enums.SagaStatus;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public interface SagaService<ID, S> {

    void setStepAndStatus(ID sagaId, S step, SagaStatus status);

    void setStatus(ID sagaId, SagaStatus status);

    S getStep(ID sagaId);

}

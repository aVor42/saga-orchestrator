package com.github.avor42.saga.orchestrator.service;

import com.github.avor42.saga.orchestrator.model.enums.SagaStatus;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public interface SagaService<E, S> {

    E setStepAndStatus(E saga, S step, SagaStatus status);

    E setStep(E saga, S step);

    E setStatus(E saga, SagaStatus status);

    S getStep(E saga);

}

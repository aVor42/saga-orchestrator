package com.github.avor42.saga.orchestrator.service;

import java.util.UUID;

import com.github.avor42.saga.orchestrator.model.enums.SagaStatus;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public interface SagaService<E, S> {

    E setStepAndStatus(UUID uuid, S step, SagaStatus status);

    E setStep(UUID uuid, S step);

    E setStatus(UUID uuid, SagaStatus status);

}

package com.github.avor42.saga.orchestrator;

import com.github.avor42.saga.orchestrator.service.SagaService;
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvidersHolder;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public interface SagaEngineTools<E, S> {

    StepMachine<S> getStepMachine();

    SagaService<E, S> getSagaService();

    SagaStepActionProvidersHolder<E, S> getStepActionProvidersHolder();

}

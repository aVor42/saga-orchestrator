package com.github.avor42.saga.orchestrator.engine;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.avor42.saga.orchestrator.machine.StepMachine;
import com.github.avor42.saga.orchestrator.service.SagaService;
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvidersRegistry;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class SagaEngineBuilder<ID, S> {

    private static void isNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <_ID, _S> SagaEngineBuilder<_ID, _S> builder(String sagaName, StepMachine<_S> stepMachine, SagaService<_ID, _S> sagaService,
            SagaStepActionProvidersRegistry<_ID, _S> sagaStepActionProvidersRegistry) {
        isNotNull(sagaName, "sagaName must not be null");
        if (sagaName.isBlank()) {
            throw new IllegalArgumentException("sagaName must not be blank");
        }

        isNotNull(stepMachine, "stepMachine must not be null");
        isNotNull(sagaService, "sagaService must not be null");
        isNotNull(sagaStepActionProvidersRegistry, "sagaStepActionProvidersRegistry must not be null");
        return new SagaEngineBuilder<>(sagaName, stepMachine, sagaService, sagaStepActionProvidersRegistry);
    }

    private final String sagaName;
    private final StepMachine<S> stepMachine;
    private final SagaService<ID, S> sagaService;
    private final SagaStepActionProvidersRegistry<ID, S> sagaStepActionProvidersRegistry;
    private Consumer<ID> sagaCompletedHandler;
    private Consumer<ID> sagaCompensationCompletedHandler;
    private BiConsumer<ID, S> compensationFailureHandler;

    private SagaEngineBuilder(String sagaName, StepMachine<S> stepMachine, SagaService<ID, S> sagaService,
            SagaStepActionProvidersRegistry<ID, S> sagaStepActionProvidersRegistry) {
        this.sagaName = sagaName;
        this.stepMachine = stepMachine;
        this.sagaService = sagaService;
        this.sagaStepActionProvidersRegistry = sagaStepActionProvidersRegistry;
    }

    public SagaEngine<ID, S> build() {
        SagaEngine<ID, S> sagaEngine = new SagaEngine<>(sagaName, stepMachine, sagaService, sagaStepActionProvidersRegistry);
        if (sagaCompletedHandler != null) {
            sagaEngine.setSagaCompletedHandler(sagaCompletedHandler);
        }

        if (sagaCompensationCompletedHandler != null) {
            sagaEngine.setSagaCompensationCompletedHandler(sagaCompensationCompletedHandler);
        }

        if (compensationFailureHandler != null) {
            sagaEngine.setCompensationFailureHandler(compensationFailureHandler);
        }

        return sagaEngine;
    }

    public SagaEngineBuilder<ID, S> setSagaCompletedHandler(Consumer<ID> sagaCompletedHandler) {
        this.sagaCompletedHandler = sagaCompletedHandler;
        return this;
    }

    public SagaEngineBuilder<ID, S> setSagaCompensationCompletedHandler(Consumer<ID> sagaCompensationCompletedHandler) {
        this.sagaCompensationCompletedHandler = sagaCompensationCompletedHandler;
        return this;
    }

    public SagaEngineBuilder<ID, S> setCompensationFailureHandler(BiConsumer<ID, S> compensationFailureHandler) {
        this.compensationFailureHandler = compensationFailureHandler;
        return this;
    }

}

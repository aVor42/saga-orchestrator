package com.github.avor42.saga.orchestrator.engine;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.avor42.saga.orchestrator.machine.StepMachine;
import com.github.avor42.saga.orchestrator.service.SagaService;
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvidersHolder;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class SagaEngineBuilder<ID, S> {

    private String sagaName;
    private StepMachine<S> stepMachine;
    private SagaService<ID, S> sagaService;
    private SagaStepActionProvidersHolder<ID, S> stepActionProvidersHolder;
    private Consumer<ID> finalCompensationHandler;
    private Consumer<ID> finalExecutionHandler;
    private BiConsumer<ID, S> failureCompensationHandler;

    SagaEngineBuilder() {
    }

    public SagaEngine<ID, S> build() {
        isNotNull(sagaName, "sagaName must not be null");
        if (sagaName.isBlank()) {
            throw new IllegalStateException("sagaName must not be blank");
        }

        isNotNull(stepMachine, "stepMachine must not be null");
        isNotNull(sagaService, "sagaService must not be null");
        isNotNull(stepActionProvidersHolder, "stepActionProvidersHolder must not be null");

        SagaEngine<ID, S> sagaEngine = new SagaEngine<>(sagaName, stepMachine, sagaService, stepActionProvidersHolder);
        if (finalExecutionHandler != null) {
            sagaEngine.setFinalExecutionHandler(finalExecutionHandler);
        }

        if (finalCompensationHandler != null) {
            sagaEngine.setFinalCompensationHandler(finalCompensationHandler);
        }

        if (failureCompensationHandler != null) {
            sagaEngine.setFailureCompensationHandler(failureCompensationHandler);
        }

        return sagaEngine;
    }

    public SagaEngineBuilder<ID, S> setSagaName(String sagaName) {
        this.sagaName = sagaName;
        return this;
    }

    public SagaEngineBuilder<ID, S> setStepMachine(StepMachine<S> stepMachine) {
        this.stepMachine = stepMachine;
        return this;
    }

    public SagaEngineBuilder<ID, S> setSagaService(SagaService<ID, S> sagaService) {
        this.sagaService = sagaService;
        return this;
    }

    public SagaEngineBuilder<ID, S> setStepActionProvidersHolder(
            SagaStepActionProvidersHolder<ID, S> stepActionProvidersHolder) {
        this.stepActionProvidersHolder = stepActionProvidersHolder;
        return this;
    }

    public SagaEngineBuilder<ID, S> setFinalCompensationHandler(Consumer<ID> finalCompensationHandler) {
        this.finalCompensationHandler = finalCompensationHandler;
        return this;
    }

    public SagaEngineBuilder<ID, S> setFinalExecutionHandler(Consumer<ID> finalExecutionHandler) {
        this.finalExecutionHandler = finalExecutionHandler;
        return this;
    }

    public SagaEngineBuilder<ID, S> setFailureCompensationHandler(BiConsumer<ID, S> failureCompensationHandler) {
        this.failureCompensationHandler = failureCompensationHandler;
        return this;
    }

    private void isNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalStateException(message);
        }
    }

}

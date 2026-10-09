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
public class SagaEngineBuilder<E, S> {

    private StepMachine<S> stepMachine;
    private SagaService<E, S> sagaService;
    private SagaStepActionProvidersHolder<E, S> stepActionProvidersHolder;
    private Consumer<E> finalCompensationHandler;
    private Consumer<E> finalExecutionHandler;
    private BiConsumer<E, S> failureCompensationHandler;

    SagaEngineBuilder() {
    }

    public SagaEngine<E, S> build() {
        isNotNull(stepMachine, "stepMachine must not be null");
        isNotNull(sagaService, "sagaService must not be null");
        isNotNull(stepActionProvidersHolder, "stepActionProvidersHolder must not be null");

        SagaEngine<E, S> sagaEngine = new SagaEngine<>(stepMachine, sagaService, stepActionProvidersHolder);
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

    public SagaEngineBuilder<E, S> setStepMachine(StepMachine<S> stepMachine) {
        this.stepMachine = stepMachine;
        return this;
    }

    public SagaEngineBuilder<E, S> setSagaService(SagaService<E, S> sagaService) {
        this.sagaService = sagaService;
        return this;
    }

    public SagaEngineBuilder<E, S> setStepActionProvidersHolder(
            SagaStepActionProvidersHolder<E, S> stepActionProvidersHolder) {
        this.stepActionProvidersHolder = stepActionProvidersHolder;
        return this;
    }

    public SagaEngineBuilder<E, S> setFinalCompensationHandler(Consumer<E> finalCompensationHandler) {
        this.finalCompensationHandler = finalCompensationHandler;
        return this;
    }

    public SagaEngineBuilder<E, S> setFinalExecutionHandler(Consumer<E> finalExecutionHandler) {
        this.finalExecutionHandler = finalExecutionHandler;
        return this;
    }

    public SagaEngineBuilder<E, S> setFailureCompensationHandler(BiConsumer<E, S> failureCompensationHandler) {
        this.failureCompensationHandler = failureCompensationHandler;
        return this;
    }

    private void isNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalStateException(message);
        }
    }

}

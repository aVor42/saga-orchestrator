package com.github.avor42.saga.orchestrator.step;

import com.github.avor42.saga.orchestrator.model.SagaStepAction;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public abstract class SagaStepActionProvider<E, S> {

    public abstract S supportedStep();

    public void process(E saga) {
        if (saga == null) {
            throw new IllegalArgumentException("Saga cannot be null");
        }

        SagaStepAction<E, S> sagaStepAction = new SagaStepAction<>(saga, supportedStep(), SagaStepActionType.PROCESSING);
        ensureAndSendProcessing(sagaStepAction);
    }

    public void compensate(E saga) {
        if (saga == null) {
            throw new IllegalArgumentException("Saga cannot be null");
        }

        SagaStepAction<E, S> sagaStepAction = new SagaStepAction<>(saga, supportedStep(), SagaStepActionType.COMPENSATION);
        ensureAndSendCompensation(sagaStepAction);
    }

    protected abstract void ensureAndSendProcessing(SagaStepAction<E, S> sagaStepAction);

    protected abstract void ensureAndSendCompensation(SagaStepAction<E, S> sagaStepAction);

}
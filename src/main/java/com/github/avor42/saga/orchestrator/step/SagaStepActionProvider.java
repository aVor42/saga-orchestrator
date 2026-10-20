package com.github.avor42.saga.orchestrator.step;

import com.github.avor42.saga.orchestrator.model.SagaStepAction;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public abstract class SagaStepActionProvider<ID, S> {

    public abstract S supportedStep();

    public void execute(ID sagaId, String sagaName) {
        if (sagaId == null) {
            throw new IllegalArgumentException("sagaId cannot be null");
        }

        if (sagaName == null || sagaName.isBlank()) {
            throw new IllegalArgumentException("sagaName cannot be empty");
        }

        SagaStepAction<ID, S> sagaStepAction = new SagaStepAction<>(sagaId, sagaName, supportedStep(), SagaStepActionType.EXECUTION);
        ensureAndSendExecution(sagaStepAction);
    }

    public void compensate(ID sagaId, String sagaName) {
        if (sagaId == null) {
            throw new IllegalArgumentException("sagaId cannot be null");
        }

        if (sagaName == null || sagaName.isBlank()) {
            throw new IllegalArgumentException("sagaName cannot be empty");
        }

        SagaStepAction<ID, S> sagaStepAction = new SagaStepAction<>(sagaId, sagaName, supportedStep(), SagaStepActionType.COMPENSATION);
        ensureAndSendCompensation(sagaStepAction);
    }

    protected abstract void ensureAndSendExecution(SagaStepAction<ID, S> sagaStepAction);

    protected abstract void ensureAndSendCompensation(SagaStepAction<ID, S> sagaStepAction);

}
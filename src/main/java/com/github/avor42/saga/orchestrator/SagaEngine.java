package com.github.avor42.saga.orchestrator;

import com.github.avor42.saga.orchestrator.model.SagaStepReply;
import com.github.avor42.saga.orchestrator.model.enums.SagaStatus;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionStatus;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvider;
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvidersHolder;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
@Slf4j
public abstract class SagaEngine<E, S> {

    public E start(E saga) {
        UUID sagaUuid = extractSagaUuid(saga);
        if (sagaUuid == null) {
            throw new IllegalStateException("Saga uuid must not be null");
        }

        S firstStep = getSagaEngineTools()
                .getStepMachine().getFirstStep();

        processStep(saga, firstStep);
        return saga;
    }

    public void processReply(SagaStepReply<E> sagaStepReply) {
        if (sagaStepReply == null) {
            throw new IllegalArgumentException("SagaStepReply must not be null");
        }

        SagaStepActionType type = sagaStepReply.type();
        if (type == null) {
            throw new IllegalArgumentException("SagaStepReply.type must not be null");
        }

        SagaStepActionStatus status = sagaStepReply.status();
        if (status == null) {
            throw new IllegalArgumentException("SagaStepReply.status must not be null");
        }

        if (SagaStepActionType.EXECUTION.equals(type)) {
            processExecutionReply(sagaStepReply);
        } else if (SagaStepActionType.COMPENSATION.equals(type)) {
            processCompensationReply(sagaStepReply);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action type: " + type);
        }
    }

    protected abstract UUID extractSagaUuid(E saga);

    protected abstract SagaEngineTools<E, S> getSagaEngineTools();

    protected void finalCompensation(E saga) {

    }

    protected void finalExecute(E saga) {

    }

    private void processExecutionReply(SagaStepReply<E> sagaStepReply) {
        if (SagaStepActionStatus.SUCCESS.equals(status)) {

        } else if (SagaStepActionStatus.FAILURE.equals(status)) {

        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepReply);
        }
    }

    private void processCompensationReply(SagaStepReply<E> sagaStepReply) {
        if (SagaStepActionStatus.SUCCESS.equals(status)) {

        } else if (SagaStepActionStatus.FAILURE.equals(status)) {

        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepReply);
        }
    }

    private void compensatePreviousStep(E saga, S step) {
        StepMachine<S> stepMachine = getSagaEngineTools().getStepMachine();
        if (stepMachine.isFirstStep(step)) {
            finalCompensation(saga);
            return;
        }

        S previousStep = stepMachine.getPreviousStep(step);
        SagaStepActionProvidersHolder<E, S> providersHolder = getSagaEngineTools().getStepActionProvidersHolder();
        SagaStepActionProvider<E, S> provider = providersHolder.getActionProvider(previousStep);
        provider.compensate(saga);
    }

    private void processStep(E saga, S step) {
        getSagaEngineTools().getSagaService()
                .setStepAndStatus(extractSagaUuid(saga), step, SagaStatus.IN_PROGRESS);
        try {
            getSagaEngineTools().getStepActionProvidersHolder()
                    .getActionProvider(step)
                    .process(saga);
        } catch (Throwable throwable) {
            log.error("Error processing saga step {}", step, throwable);
            compensatePreviousStep(saga, step);
        }
    }

}

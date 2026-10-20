package com.github.avor42.saga.orchestrator.engine;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.avor42.saga.orchestrator.machine.StepMachine;
import com.github.avor42.saga.orchestrator.model.SagaStepActionReply;
import com.github.avor42.saga.orchestrator.model.enums.SagaStatus;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionStatus;
import com.github.avor42.saga.orchestrator.model.enums.SagaStepActionType;
import com.github.avor42.saga.orchestrator.service.SagaService;
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvidersRegistry;

import lombok.Getter;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class SagaEngine<ID, S> {

    @Getter
    private final String sagaName;
    private final StepMachine<S> stepMachine;
    private final SagaService<ID, S> sagaService;
    private final SagaStepActionProvidersRegistry<ID, S> sagaStepActionProvidersRegistry;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Consumer<ID> sagaCompletedHandler;
    private Consumer<ID> sagaCompensationCompletedHandler;
    private BiConsumer<ID, S> compensationFailureHandler;

    SagaEngine(String sagaName, StepMachine<S> stepMachine, SagaService<ID, S> sagaService,
            SagaStepActionProvidersRegistry<ID, S> sagaStepActionProvidersRegistry) {
        this.sagaName = sagaName;
        this.stepMachine = stepMachine;
        this.sagaService = sagaService;
        this.sagaStepActionProvidersRegistry = sagaStepActionProvidersRegistry;
    }

    public void startSaga(ID sagaId) {
        if (sagaId == null) {
            throw new IllegalArgumentException("sagaId must not be null");
        }

        S firstStep = stepMachine.getFirstStep();

        executeStep(sagaId, firstStep);
    }

    public void handleActionReply(SagaStepActionReply<ID, S> sagaStepActionReply) {
        if (sagaStepActionReply == null) {
            throw new IllegalArgumentException("SagaStepActionReply must not be null");
        }

        SagaStepActionType type = sagaStepActionReply.type();
        if (type == null) {
            throw new IllegalArgumentException("SagaStepActionReply.type must not be null");
        }

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (status == null) {
            throw new IllegalArgumentException("SagaStepActionReply.status must not be null");
        }

        if (type == SagaStepActionType.EXECUTION) {
            handleExecutionReply(sagaStepActionReply);
        } else if (type == SagaStepActionType.COMPENSATION) {
            handleCompensationReply(sagaStepActionReply);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action type: " + type);
        }
    }

    void setSagaCompletedHandler(Consumer<ID> handler) {
        this.sagaCompletedHandler = handler;
    }

    void setSagaCompensationCompletedHandler(Consumer<ID> handler) {
        this.sagaCompensationCompletedHandler = handler;
    }

    void setCompensationFailureHandler(BiConsumer<ID, S> handler) {
        this.compensationFailureHandler = handler;
    }

    private void notifyCompensationCompleted(ID sagaId) {
        if (sagaCompensationCompletedHandler != null) {
            sagaCompensationCompletedHandler.accept(sagaId);
        }
    }

    private void notifySagaCompleted(ID sagaId) {
        if (sagaCompletedHandler != null) {
            sagaCompletedHandler.accept(sagaId);
        }
    }

    private void handleCompensationFailure(ID sagaId, S step) {
        if (compensationFailureHandler != null) {
            compensationFailureHandler.accept(sagaId, step);
        }
    }

    private void handleExecutionReply(SagaStepActionReply<ID, S> sagaStepActionReply) {
        ID sagaId = sagaStepActionReply.sagaId();
        S replyStep = sagaStepActionReply.step();
        S savedStep = sagaService.getStep(sagaStepActionReply.sagaId());
        if (!Objects.equals(replyStep, savedStep)) {
            throw new IllegalStateException(
                    "Saga step mismatch for saga (name=" + sagaName + ", id=" + sagaId + ")"
                            + ": saved step " + savedStep
                            + ", but received reply for step " + replyStep
            );
        }

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            advanceToNextStep(sagaId, replyStep);
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {
            compensatePreviousStep(sagaId, replyStep);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepActionReply);
        }
    }

    private void advanceToNextStep(ID sagaId, S step) {
        if (stepMachine.isLastStep(step)) {
            sagaService.setStepAndStatus(sagaId, step, SagaStatus.COMPLETED);
            notifySagaCompleted(sagaId);
        } else {
            S nextStep = stepMachine.getNextStep(step);
            executeStep(sagaId, nextStep);
        }
    }

    private void executeStep(ID sagaId, S step) {
        sagaService.setStepAndStatus(sagaId, step, SagaStatus.IN_PROGRESS);
        try {
            sagaStepActionProvidersRegistry.getActionProvider(step).execute(sagaId, sagaName);
        } catch (Exception e) {
            log.error("Error executing saga (name={}, id={}) step {}", sagaName, sagaId, step, e);
            compensatePreviousStep(sagaId, step);
        }
    }

    private void handleCompensationReply(SagaStepActionReply<ID, S> sagaStepActionReply) {
        ID sagaId = sagaStepActionReply.sagaId();
        S step = sagaStepActionReply.step();

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (status == SagaStepActionStatus.SUCCESS) {
            compensatePreviousStep(sagaId, step);
        } else if (status == SagaStepActionStatus.FAILURE) {
            handleCompensationFailure(sagaId, step);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepActionReply);
        }
    }

    private void compensatePreviousStep(ID sagaId, S step) {
        if (stepMachine.isFirstStep(step)) {
            sagaService.setStatus(sagaId, SagaStatus.FAILED);
            notifyCompensationCompleted(sagaId);
            return;
        }

        S previousStep = stepMachine.getPreviousStep(step);
        compensateStep(sagaId, previousStep);
    }

    private void compensateStep(ID sagaId, S step) {
        try {
            sagaStepActionProvidersRegistry.getActionProvider(step).compensate(sagaId, sagaName);
        } catch (Exception e) {
            log.error("Error compensating saga (name={}, id={}) step {}", sagaName, sagaId, step, e);
            compensatePreviousStep(sagaId, step);
        }
    }

}

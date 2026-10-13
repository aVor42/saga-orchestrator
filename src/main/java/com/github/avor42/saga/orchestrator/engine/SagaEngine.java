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
import com.github.avor42.saga.orchestrator.step.SagaStepActionProvidersHolder;

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
    private final SagaStepActionProvidersHolder<ID, S> stepActionProvidersHolder;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Consumer<ID> finalCompensationHandler;
    private Consumer<ID> finalExecutionHandler;
    private BiConsumer<ID, S> failureCompensationHandler;

    SagaEngine(String sagaName, StepMachine<S> stepMachine, SagaService<ID, S> sagaService, SagaStepActionProvidersHolder<ID, S> stepActionProvidersHolder) {
        this.sagaName = sagaName;
        this.stepMachine = stepMachine;
        this.sagaService = sagaService;
        this.stepActionProvidersHolder = stepActionProvidersHolder;
    }

    public void start(ID sagaId) {
        if (sagaId == null) {
            throw new IllegalStateException("sagaId must not be null");
        }

        S firstStep = stepMachine.getFirstStep();

        processStep(sagaId, firstStep);
    }

    public void processReply(SagaStepActionReply<ID, S> sagaStepActionReply) {
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

        if (SagaStepActionType.PROCESSING.equals(type)) {
            processExecutionReply(sagaStepActionReply);
        } else if (SagaStepActionType.COMPENSATION.equals(type)) {
            processCompensationReply(sagaStepActionReply);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action type: " + type);
        }
    }

    void setFinalCompensationHandler(Consumer<ID> handler) {
        this.finalCompensationHandler = handler;
    }

    void setFinalExecutionHandler(Consumer<ID> handler) {
        this.finalExecutionHandler = handler;
    }

    void setFailureCompensationHandler(BiConsumer<ID, S> handler) {
        this.failureCompensationHandler = handler;
    }

    private void finalCompensate(ID sagaId) {
        if (finalCompensationHandler != null) {
            finalCompensationHandler.accept(sagaId);
        }
    }

    private void finalExecute(ID sagaId) {
        if (finalExecutionHandler != null) {
            finalExecutionHandler.accept(sagaId);
        }
    }

    private void handleFailureCompensation(ID sagaId, S step) {
        if (failureCompensationHandler != null) {
            failureCompensationHandler.accept(sagaId, step);
        }
    }

    private void processExecutionReply(SagaStepActionReply<ID, S> sagaStepActionReply) {
        ID sagaId = sagaStepActionReply.sagaId();
        S replyStep = sagaStepActionReply.step();
        S savedStep = sagaService.getStep(sagaStepActionReply.sagaId());
        if (!Objects.equals(replyStep, savedStep)) {
            throw new IllegalStateException("Missing consistence. Saga " + sagaId + " has " + savedStep
                    + ", but provide for process " + replyStep + " from reply");
        }

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            nextStep(sagaId, replyStep);
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {
            compensatePreviousStep(sagaId, replyStep);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepActionReply);
        }
    }

    private void processCompensationReply(SagaStepActionReply<ID, S> sagaStepActionReply) {
        ID sagaId = sagaStepActionReply.sagaId();
        S step = sagaStepActionReply.step();

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            compensatePreviousStep(sagaId, step);
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {
            handleFailureCompensation(sagaId, step);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepActionReply);
        }
    }

    private void nextStep(ID sagaId, S step) {
        if (stepMachine.isLastStep(step)) {
            finalExecute(sagaId);
            sagaService.setStepAndStatus(sagaId, step, SagaStatus.COMPLETED);
        } else {
            S nextStep = stepMachine.getNextStep(step);
            processStep(sagaId, nextStep);
        }
    }

    private void processStep(ID sagaId, S step) {
        sagaService.setStepAndStatus(sagaId, step, SagaStatus.IN_PROGRESS);
        try {
            stepActionProvidersHolder.getActionProvider(step).process(sagaId, sagaName);
        } catch (Throwable throwable) {
            log.error("Error processing saga {} step {}", sagaId, step, throwable);
            compensatePreviousStep(sagaId, step);
        }
    }

    private void compensatePreviousStep(ID sagaId, S step) {
        if (stepMachine.isFirstStep(step)) {
            finalCompensate(sagaId);
            sagaService.setStatus(sagaId, SagaStatus.FAILED);
            return;
        }

        S previousStep = stepMachine.getPreviousStep(step);
        compensateStep(sagaId, previousStep);
    }

    private void compensateStep(ID sagaId, S step) {
        try {
            stepActionProvidersHolder.getActionProvider(step).compensate(sagaId, sagaName);
        } catch (Throwable throwable) {
            log.error("Error compensate saga {} step {}", sagaId, step, throwable);
            compensatePreviousStep(sagaId, step);
        }
    }

}

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

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class SagaEngine<E, S> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final StepMachine<S> stepMachine;
    private final SagaService<E, S> sagaService;
    private final SagaStepActionProvidersHolder<E, S> stepActionProvidersHolder;

    private Consumer<E> finalCompensationHandler;
    private Consumer<E> finalExecutionHandler;
    private BiConsumer<E, S> failureCompensationHandler;

    SagaEngine(StepMachine<S> stepMachine, SagaService<E, S> sagaService, SagaStepActionProvidersHolder<E, S> stepActionProvidersHolder) {
        this.stepMachine = stepMachine;
        this.sagaService = sagaService;
        this.stepActionProvidersHolder = stepActionProvidersHolder;
    }

    public E start(E saga) {
        if (saga == null) {
            throw new IllegalStateException("Saga must not be null");
        }

        S firstStep = stepMachine.getFirstStep();

        processStep(saga, firstStep);
        return saga;
    }

    public void processReply(SagaStepActionReply<E, S> sagaStepActionReply) {
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

    void setFinalCompensationHandler(Consumer<E> handler) {
        this.finalCompensationHandler = handler;
    }

    void setFinalExecutionHandler(Consumer<E> handler) {
        this.finalExecutionHandler = handler;
    }

    void setFailureCompensationHandler(BiConsumer<E, S> handler) {
        this.failureCompensationHandler = handler;
    }

    private void finalCompensate(E saga) {
        if (finalCompensationHandler != null) {
            finalCompensationHandler.accept(saga);
        }
    }

    private void finalExecute(E saga) {
        if (finalExecutionHandler != null) {
            finalExecutionHandler.accept(saga);
        }
    }

    private void handleFailureCompensation(E saga, S step) {
        if (failureCompensationHandler != null) {
            failureCompensationHandler.accept(saga, step);
        }
    }

    private void processExecutionReply(SagaStepActionReply<E, S> sagaStepActionReply) {
        E saga = sagaStepActionReply.saga();
        S replyStep = sagaStepActionReply.step();
        S savedStep = sagaService.getStep(sagaStepActionReply.saga());
        if (!Objects.equals(replyStep, savedStep)) {
            throw new IllegalStateException("Missing consistence. Saga " + saga + " has " + savedStep
                    + ", but provide for process " + replyStep + " from reply");
        }

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            nextStep(saga, replyStep);
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {
            compensatePreviousStep(saga, replyStep);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepActionReply);
        }
    }

    private void processCompensationReply(SagaStepActionReply<E, S> sagaStepActionReply) {
        E saga = sagaStepActionReply.saga();
        S step = sagaStepActionReply.step();

        SagaStepActionStatus status = sagaStepActionReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            compensatePreviousStep(saga, step);
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {
            handleFailureCompensation(saga, step);
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepActionReply);
        }
    }

    private void nextStep(E saga, S step) {
        if (stepMachine.isLastStep(step)) {
            finalExecute(saga);
            sagaService.setStepAndStatus(saga, step, SagaStatus.COMPLETED);
        } else {
            S nextStep = stepMachine.getNextStep(step);
            processStep(saga, nextStep);
        }
    }

    private void processStep(E saga, S step) {
        sagaService.setStepAndStatus(saga, step, SagaStatus.IN_PROGRESS);
        try {
            stepActionProvidersHolder.getActionProvider(step).process(saga);
        } catch (Throwable throwable) {
            log.error("Error processing saga {} step {}", saga, step, throwable);
            compensatePreviousStep(saga, step);
        }
    }

    private void compensatePreviousStep(E saga, S step) {
        if (stepMachine.isFirstStep(step)) {
            finalCompensate(saga);
            sagaService.setStatus(saga, SagaStatus.FAILED);
            return;
        }

        S previousStep = stepMachine.getPreviousStep(step);
        compensateStep(saga, previousStep);
    }

    private void compensateStep(E saga, S step) {
        try {
            stepActionProvidersHolder.getActionProvider(step).compensate(saga);
        } catch (Throwable throwable) {
            log.error("Error compensate saga saga {} step {}", saga, step, throwable);
            compensatePreviousStep(saga, step);
        }
    }

}

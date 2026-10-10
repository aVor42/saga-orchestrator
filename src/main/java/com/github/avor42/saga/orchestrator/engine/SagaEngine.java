package com.github.avor42.saga.orchestrator.engine;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.avor42.saga.orchestrator.machine.StepMachine;
import com.github.avor42.saga.orchestrator.model.SagaStepReply;
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
    private Consumer<E> failureCompensationHandler;

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

    public void processReply(SagaStepReply<E, S> sagaStepReply) {
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

    void setFinalCompensationHandler(Consumer<E> handler) {
        this.finalCompensationHandler = handler;
    }

    void setFinalExecutionHandler(Consumer<E> handler) {
        this.finalExecutionHandler = handler;
    }

    void setFailureCompensationHandler(Consumer<E> handler) {
        this.failureCompensationHandler = handler;
    }

    protected void finalCompensate(E saga) {
        if(finalCompensationHandler != null) {
            finalCompensationHandler.accept(saga);
        }
    }

    protected void finalExecute(E saga) {
        if(finalExecutionHandler != null){
            finalExecutionHandler.accept(saga);
        }
    }

    private void processExecutionReply(SagaStepReply<E, S> sagaStepReply) {
        SagaStepActionStatus status = sagaStepReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            nextStep(sagaStepReply.saga(), sagaStepReply.step());
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {
            compensatePreviousStep(sagaStepReply.saga(), sagaStepReply.step());
        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepReply);
        }
    }

    private void processCompensationReply(SagaStepReply<E, S> sagaStepReply) {
        SagaStepActionStatus status = sagaStepReply.status();
        if (SagaStepActionStatus.SUCCESS.equals(status)) {
            compensatePreviousStep(sagaStepReply.saga(), sagaStepReply.step());
        } else if (SagaStepActionStatus.FAILURE.equals(status)) {

        } else {
            throw new IllegalStateException("Can't process reply. Unknown action reply status: " + status + " for " + sagaStepReply);
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
            log.error("Error processing saga step {}", step, throwable);
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
            log.error("Error processing saga step {}", step, throwable);
            compensatePreviousStep(saga, step);
        }
    }

}

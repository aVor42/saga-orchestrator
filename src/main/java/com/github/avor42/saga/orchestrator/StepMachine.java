package com.github.avor42.saga.orchestrator;

public interface StepMachine<S> {

    boolean isFirstStep(S step);

    S getFirstStep();

    boolean isLastStep(S step);

    S getLastStep();

    S getNextStep(S step);

    S getPreviousStep(S step);

}

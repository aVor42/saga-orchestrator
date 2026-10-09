package com.github.avor42.saga.orchestrator;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public interface StepMachine<S> {

    boolean isFirstStep(S step);

    S getFirstStep();

    boolean isLastStep(S step);

    S getLastStep();

    S getNextStep(S step);

    S getPreviousStep(S step);

}

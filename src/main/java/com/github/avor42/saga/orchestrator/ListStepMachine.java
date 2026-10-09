package com.github.avor42.saga.orchestrator;

import java.util.List;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public abstract class ListStepMachine<S> implements StepMachine<S> {

    @Override
    public boolean isFirstStep(S step) {
        validateStep(step);
        return getFirstStep().equals(step);
    }

    @Override
    public S getFirstStep() {
        return getValidatedSteps().getFirst();
    }

    @Override
    public boolean isLastStep(S step) {
        validateStep(step);
        return getLastStep().equals(step);
    }

    @Override
    public S getLastStep() {
        return getValidatedSteps().getLast();
    }

    @Override
    public S getNextStep(S step) {
        validateStep(step);

        List<S> steps = getValidatedSteps();
        int index = steps.indexOf(step);

        if (index < 0) {
            throw new IllegalArgumentException("Unknown step " + step);
        }

        if (++index < steps.size()) {
            return steps.get(index);
        }

        throw new ArrayIndexOutOfBoundsException("No steps in list");
    }

    @Override
    public S getPreviousStep(S step) {
        validateStep(step);

        List<S> steps = getValidatedSteps();
        int index = steps.indexOf(step);

        if (index < 0) {
            throw new IllegalArgumentException("Unknown step " + step);
        }

        if (--index >= 0) {
            return steps.get(index);
        }

        throw new ArrayIndexOutOfBoundsException("No steps in list");
    }

    protected void validateStep(S step) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
    }

    protected abstract List<S> getSteps();

    private List<S> getValidatedSteps() {
        List<S> steps = getSteps();
        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException("Steps list must not be empty");
        }

        return steps;
    }

}

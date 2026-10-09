package com.github.avor42.saga.orchestrator.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public abstract class SagaStepActionProvidersHolder<E, S> {

    protected final Map<S, SagaStepActionProvider<E, S>> actionsProvidersMap;

    public SagaStepActionProvidersHolder(List<SagaStepActionProvider<E, S>> actionsProviders) {
        if (actionsProviders == null) {
            throw new IllegalArgumentException("actions must not be null");
        }

        actionsProvidersMap = new HashMap<>();
        for (SagaStepActionProvider<E, S> actionProvider : actionsProviders) {
            S supportedStep = actionProvider.supportedStep();
            if (supportedStep == null) {
                throw new IllegalStateException("Provider " + actionProvider + " has no supported step");
            }

            if (actionsProvidersMap.containsKey(supportedStep)) {
                throw new IllegalStateException("Duplicate supported saga step " + supportedStep + " in provider " + actionProvider);
            }

            actionsProvidersMap.put(supportedStep, actionProvider);
        }
    }

    public SagaStepActionProvider<E, S> getActionProvider(S step) {
        if (!actionsProvidersMap.containsKey(step)) {
            throw new IllegalArgumentException(String.format("action %s not supported", step));
        }
        return actionsProvidersMap.get(step);
    }

}

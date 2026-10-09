package com.github.avor42.saga.orchestrator.step;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public interface SagaStepActionProvider<E, S> {

    S supportedStep();

    void process(E entity);

    void compensate(E entity);

}

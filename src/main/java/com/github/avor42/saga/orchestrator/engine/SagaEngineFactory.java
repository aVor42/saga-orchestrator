package com.github.avor42.saga.orchestrator.engine;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class SagaEngineFactory {

    public static <E, S> SagaEngineBuilder<E, S> create() {
        return new SagaEngineBuilder<>();
    }

    public static <E, S> SagaEngineBuilder<E, S> create(Class<E> sagaEntityClass, Class<S> stepClass) {
        return new SagaEngineBuilder<>();
    }

}
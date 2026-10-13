package com.github.avor42.saga.orchestrator.engine;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class SagaEngineFactory {

    public static <ID, S> SagaEngineBuilder<ID, S> create() {
        return new SagaEngineBuilder<>();
    }

    public static <ID, S> SagaEngineBuilder<ID, S> create(Class<ID> sagaIdClass, Class<S> stepClass) {
        return new SagaEngineBuilder<>();
    }

}
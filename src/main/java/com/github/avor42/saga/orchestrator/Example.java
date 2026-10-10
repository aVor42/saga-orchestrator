package com.github.avor42.saga.orchestrator;

import java.util.UUID;

import com.github.avor42.saga.orchestrator.engine.SagaEngine;
import com.github.avor42.saga.orchestrator.engine.SagaEngineFactory;

/**
 * @author Voronkov A
 * @since 10.10.2026
 */
public class Example {

    private SagaEngine<String, UUID> test2() {
        return SagaEngineFactory.create(String.class, UUID.class)
                .setFinalExecutionHandler(null)
                .setFinalCompensationHandler(null)
                .build();
    }

}

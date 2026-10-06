package com.github.avor42.saga.orchestrator;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author Voronkov A
 * @since 07.10.2026
 */
public class SagaEngine<S> {

    private final StepMachine<S> stepMachine;

    public SagaEngine(StepMachine<S> stepMachine){
        this.stepMachine = stepMachine;
    }

    @Transactional
    public UUID start() {
        return UUID.randomUUID();
    }


    public void nextStep() {
        System.out.println("Next step");
    }

}

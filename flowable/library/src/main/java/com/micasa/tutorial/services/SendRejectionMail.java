package com.micasa.tutorial.services;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class SendRejectionMail implements JavaDelegate {

    public void execute(DelegateExecution execution) {
        System.out.println("Send rejection email for employee "
                + execution.getVariable("employee"));
    }

}
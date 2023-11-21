package com.micasa.tutorial;

import org.flowable.bpmn.model.Activity;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@FlowableTest // Same as @ExtendWith(FlowableExtension.class)
class HolidayRequestProcessTest {

    private RuntimeService runtimeService;
    private TaskService taskService;
    private HistoryService historyService;

    @BeforeEach
    void setUp(ProcessEngine processEngine) {
        this.runtimeService = processEngine.getRuntimeService();
        this.taskService = processEngine.getTaskService();
        this.historyService = processEngine.getHistoryService();
    }

    @Test
    @Deployment(resources = "Sales-Flow.bpmn20.xml")
    @DisplayName("Holiday approved")
    void holidayApproved() {
        runtimeService.startProcessInstanceByKey("Sales-Process", Map.of("employee", "Aowss Ibrahim", "nrOfHolidays", 5));

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getTaskDefinitionKey(), is("approveTask"));

        taskService.complete(task.getId(), Map.of("approved", true));

        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getTaskDefinitionKey(), is("holidayApprovedTask"));

        taskService.complete(task.getId());

        assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));

        var activities = historyService.createHistoricActivityInstanceQuery().list();
        var serviceTasks = activities.stream()
                .filter(activity -> activity.getActivityType().equals("serviceTask"))
                .map(HistoricActivityInstance::getActivityId)
                .toList();
        assertThat(serviceTasks, is(List.of("externalSystemCall")));
    }

    @Test
    @Deployment(resources = "holiday-request.bpmn")
    @DisplayName("Holiday rejected")
    void holidayRejected() {
        runtimeService.startProcessInstanceByKey("holidayRequest", Map.of("employee", "Aowss Ibrahim", "nrOfHolidays", 50));

        Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getTaskDefinitionKey(), is("approveTask"));

        taskService.complete(task.getId(), Map.of("approved", false));

        assertThat(runtimeService.createProcessInstanceQuery().count(), is(0L));

        var activities = historyService.createHistoricActivityInstanceQuery().list();
        var serviceTasks = activities.stream()
                .filter(activity -> activity.getActivityType().equals("serviceTask"))
                .map(HistoricActivityInstance::getActivityId)
                .toList();
        assertThat(serviceTasks, is(List.of("sendRejectionMail")));
    }
}
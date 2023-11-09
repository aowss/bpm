package com.micasa.tutorial;

import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Application {
    record JDBCConfiguration(String url, String username, String password, String driver) {}
    public static ProcessEngineConfiguration configure(JDBCConfiguration jdbcConfiguration) {
        return new StandaloneProcessEngineConfiguration()
                .setJdbcUrl(jdbcConfiguration.url)
                .setJdbcUsername(jdbcConfiguration.username)
                .setJdbcPassword(jdbcConfiguration.password)
                .setJdbcDriver(jdbcConfiguration.driver)
                .setDatabaseSchemaUpdate(AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE); // The schema is created if it doesn't exist
    }

    public static void main(String[] args) {
        //  Configure process engine
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE); // The schema is created if it doesn't exist

        ProcessEngine processEngine = cfg.buildProcessEngine();

        //  Storing the process file in the DB
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService
                .createDeployment()
                .addClasspathResource("holiday-request.bpmn")
                .deploy();

        //  Retrieve the process definition from the DB
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());

        //  Gather user input
        Scanner scanner= new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();

        //  Start a process instance with the given data
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("holidayRequest", variables);

        //  At this point the process is in a wait state at the user task ( the API call is synchronous and returns at wait states )

        //  Retrieve tasks list
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        tasks.forEach(task -> System.out.println(task.getName() + " for " + task.getAssignee() + " is suspended ? " + task.isSuspended()));

        //  Retrieve task details
        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");

        //  Task handling
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        variables = new HashMap<String, Object>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);

        //  History
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
//                        .processInstanceId(processInstance.getId())
//                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        activities.forEach(activity -> System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds"));
    }
}

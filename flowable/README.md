# Flowable Example

The example uses the [Open Source version of the Flowable engine](https://www.flowable.com/open-source) and follows [this documentation](https://www.flowable.com/open-source/docs/bpmn/ch02-GettingStarted).

# Deployment

As explained in the `Using Flowable` section of the [documentation](https://www.flowable.com/open-source-code), Flowable can be used in several different ways:

* embedded

The engine can be embedded in your application by including the Flowable library, which is available as a JAR.

```xml
<dependency> 
  <groupId>org.flowable</groupId> 
  <artifactId>flowable-engine</artifactId> 
  <version>7.0.0</version> 
</dependency>
```

It can therefore be embedded in a Java SE application or in any other type of application and in particular in a Spring Boot application.  

For Spring Boot applications, a Spring Boot starter project exists: [`flowable-spring-boot-started`](https://mvnrepository.com/artifact/org.flowable/flowable-spring-boot-starter).

* standalone

A [docker image](https://hub.docker.com/r/flowable/flowable-rest) is available.  

In this case, the communication with the Flowable engine is done using its REST API.

## BPMN

Human tasks are assigned to groups using the following extension: `flowable:candidateGroups="..."` or to specific users using the following extension: ` flowable:assignee="..."`.  
The value can be static, e.g. `flowable:candidateGroups="..."` or dynamic, e.g. `flowable:assignee="${employee}"`.

Service tasks are linked to implementations using the following extension: `flowable:class="..."`.  
The value is the fully qualified name of a class that implemented the `JavaDelegate` interface.
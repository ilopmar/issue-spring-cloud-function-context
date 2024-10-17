# Issue with `spring-cloud-function-context` 4.1.3

This repository reproduces one issue we had when upgrading `org.springframework.cloud:spring-cloud-dependencies` from `2023.0.1` to `2023.0.3`. It keeps the same code and configuration we have in our project, but I've tried to simplify everything as much as possible.

This happens in both Spring Boot 3.2.10 (like this project) but also in the latest Spring Boot 3.3.4.

The problem has been tracked down to `spring-cloud-function-context:4.1.3`. You can take a look to `build.gradle` to change the versions of the dependency. Initially the project is configured with the version `2023.0.3` (that pulls `4.1.3`) that reproduces the problem. Change it to `2023.0.2` to check the original behaviour.

## Error deserializing a json payload received by Kafka

The classes in `dto` subpackage are generated automatically based on async-api spec. I've just copied them here and simplify as much as I could, but all of them are necessary (I think) because we need a hierarchy of objects to reproduce the issue.

The problem happens because we have the following automatic generated DTOs hierarchy:

- `ReleaseTopicMessageDTO.Payload`: It's an interface with a @JsonSubTypes with different implementations.
- `OvaReleaseEventDTO`: One implementation of the interface (more in my real code)
  - This class has some common properties
  - OvaDTO: extra property with the data related to OVAs (this example)
    - Contains an `Image` interface (with different implementations in my real code) and two extra properties
      - ImageUriDTO: One implementation of the `Image` interface with just one property (to simplify everything)

My guess is that there's an issue with those nested interfaces `Payload` and `Image` when trying to deserialize the json event to the java objects.

### Steps to reproduce the issue:

- Start docker-compose environment `docker-compose.yml`. It starts Kafka and a Kafka UI viewer to see topics and messages.
- Start the application
- Open the Kafka UI http://localhost:8081 and go to `Topics`
- On the UI select topic `app.release`, click on the magnifying glass icon on the right.
- Click on the option "Produce to topic" (on the bottom right)
- Send the json message
    ```json
    {
      "type": "OVA",
      "ova": {
        "image": {
          "image_type": "IMAGE_URI",
          "location": "s3://xxxxxxxxx/bitnami-neo4j-5.24.2-r0-debian-12-amd64.ova"
        }
      },
      "emitted_on": "2024-10-16T09:59:03.561548427Z"
    }
    ```

With version 4.1.2 of `spring-cloud-function-context` the message is deserialized properly, the `ReleaseEventHandler` is called, and a log message is displayed on the console.

```
==================== ReleaseEventHandler.accept ====================
If you can see this message it means the deserialization worked

OvaReleaseEventDTO{ova=OvaDTO{image=ImageUriDTO{location='s3://xxxxxxxxx/bitnami-neo4j-5.24.2-r0-debian-12-amd64.ova'}, imageType='null'}, type=OVA, emittedOn=2024-10-16T09:59:03.561548427Z}
==================== ReleaseEventHandler.accept ====================
```

With version 4.1.3 we have an exception:

```
org.springframework.kafka.listener.ListenerExecutionFailedException: Listener failed
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.decorateException(KafkaMessageListenerContainer.java:2961) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:2902) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:2866) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.lambda$doInvokeRecordListener$55(KafkaMessageListenerContainer.java:2783) ~[spring-kafka-3.1.9.jar:3.1.9]
    at io.micrometer.observation.Observation.observe(Observation.java:565) ~[micrometer-observation-1.12.10.jar:1.12.10]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:2781) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:2631) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:2517) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:2155) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeIfHaveRecords(KafkaMessageListenerContainer.java:1495) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:1460) ~[spring-kafka-3.1.9.jar:3.1.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:1330) ~[spring-kafka-3.1.9.jar:3.1.9]
    at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java:1804) ~[na:na]
    at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: org.springframework.kafka.KafkaException: Failed to execute runnable
    at org.springframework.integration.kafka.inbound.KafkaInboundEndpoint.doWithRetry(KafkaInboundEndpoint.java:82) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.onMessage(KafkaMessageDrivenChannelAdapter.java:457) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.onMessage(KafkaMessageDrivenChannelAdapter.java:422) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:2888) ~[spring-kafka-3.1.9.jar:3.1.9]
    ... 12 common frames omitted
Caused by: org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@3fe9c146]
    at org.springframework.integration.support.utils.IntegrationUtils.wrapInHandlingExceptionIfNecessary(IntegrationUtils.java:191) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.handler.AbstractMessageHandler.doHandleMessage(AbstractMessageHandler.java:108) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:73) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.dispatcher.AbstractDispatcher.tryOptimizedDispatch(AbstractDispatcher.java:132) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.dispatcher.UnicastingDispatcher.doDispatch(UnicastingDispatcher.java:133) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.dispatcher.UnicastingDispatcher.dispatch(UnicastingDispatcher.java:106) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.channel.AbstractSubscribableChannel.doSend(AbstractSubscribableChannel.java:72) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.channel.AbstractMessageChannel.sendInternal(AbstractMessageChannel.java:390) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:334) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:304) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:187) ~[spring-messaging-6.1.13.jar:6.1.13]
    at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:166) ~[spring-messaging-6.1.13.jar:6.1.13]
    at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:47) ~[spring-messaging-6.1.13.jar:6.1.13]
    at org.springframework.messaging.core.AbstractMessageSendingTemplate.send(AbstractMessageSendingTemplate.java:109) ~[spring-messaging-6.1.13.jar:6.1.13]
    at org.springframework.integration.endpoint.MessageProducerSupport.lambda$sendMessage$1(MessageProducerSupport.java:262) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at io.micrometer.observation.Observation.observe(Observation.java:499) ~[micrometer-observation-1.12.10.jar:1.12.10]
    at org.springframework.integration.endpoint.MessageProducerSupport.sendMessage(MessageProducerSupport.java:262) ~[spring-integration-core-6.2.9.jar:6.2.9]
    at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.sendMessageIfAny(KafkaMessageDrivenChannelAdapter.java:391) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.lambda$onMessage$0(KafkaMessageDrivenChannelAdapter.java:460) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    at org.springframework.integration.kafka.inbound.KafkaInboundEndpoint.lambda$doWithRetry$0(KafkaInboundEndpoint.java:77) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:344) ~[spring-retry-2.0.9.jar:na]
    at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:233) ~[spring-retry-2.0.9.jar:na]
    at org.springframework.integration.kafka.inbound.KafkaInboundEndpoint.doWithRetry(KafkaInboundEndpoint.java:70) ~[spring-integration-kafka-6.2.9.jar:6.2.9]
    ... 15 common frames omitted
Caused by: java.lang.ClassCastException: class [B cannot be cast to class com.example.issue.dto.ReleaseTopicMessageDTO$Payload ([B is in module java.base of loader 'bootstrap'; com.example.issue.dto.ReleaseTopicMessageDTO$Payload is in unnamed module of loader 'app')
    at com.example.issue.ReleaseEventHandler.accept(ReleaseEventHandler.java:7) ~[main/:na]
    at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.invokeConsumer(SimpleFunctionRegistry.java:1063) ~[spring-cloud-function-context-4.1.3.jar:4.1.3]
    at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.doApply(SimpleFunctionRegistry.java:761) ~[spring-cloud-function-context-4.1.3.jar:4.1.3]
    at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.apply(SimpleFunctionRegistry.java:592) ~[spring-cloud-function-context-4.1.3.jar:4.1.3]
    at org.springframework.cloud.stream.function.PartitionAwareFunctionWrapper.apply(PartitionAwareFunctionWrapper.java:92) ~[spring-cloud-stream-4.1.3.jar:4.1.3]
    at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionWrapper.apply(FunctionConfiguration.java:823) ~[spring-cloud-stream-4.1.3.jar:4.1.3]
    at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1.handleMessageInternal(FunctionConfiguration.java:654) ~[spring-cloud-stream-4.1.3.jar:4.1.3]
    at org.springframework.integration.handler.AbstractMessageHandler.doHandleMessage(AbstractMessageHandler.java:105) ~[spring-integration-core-6.2.9.jar:6.2.9]
    ... 36 common frames omitted
```

package com.shop.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue orderQueue(@Value("${app.messaging.queues.new}") String queueName) {
        // Queue with new orders coming from the storefront service
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public TopicExchange orderEventsExchange(@Value("${app.messaging.exchanges.events}") String exchangeName) {
        // Topic exchange for routing confirmed/canceled events to different services
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean(name = "confirmedOrdersQueue")
    public Queue confirmedOrdersQueue(@Value("${app.messaging.queues.confirmed}") String queueName) {
        // Queue that procurement service will later read for confirmed orders
        return QueueBuilder.durable(queueName).build();
    }

    @Bean(name = "canceledOrdersQueue")
    public Queue canceledOrdersQueue(@Value("${app.messaging.queues.canceled}") String queueName) {
        // Queue dedicated to QA/quality team for canceled orders
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding confirmedBinding(@Qualifier("confirmedOrdersQueue") Queue confirmedOrdersQueue,
                                    TopicExchange orderEventsExchange,
                                    @Value("${app.messaging.routing.confirmed}") String routingKey) {
        // Route confirmed order events into the procurement queue
        return BindingBuilder.bind(confirmedOrdersQueue).to(orderEventsExchange).with(routingKey);
    }

    @Bean
    public Binding canceledBinding(@Qualifier("canceledOrdersQueue") Queue canceledOrdersQueue,
                                   TopicExchange orderEventsExchange,
                                   @Value("${app.messaging.routing.canceled}") String routingKey) {
        // Route canceled order events into the quality queue
        return BindingBuilder.bind(canceledOrdersQueue).to(orderEventsExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        // Serialize Rabbit messages as JSON so both services share the format
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

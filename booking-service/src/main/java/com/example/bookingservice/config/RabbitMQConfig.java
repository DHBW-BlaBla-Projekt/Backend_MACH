package com.example.bookingservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.booking}")
    private String queueName;

    @Value("${app.rabbitmq.routing.key.incoming}")
    private String incomingRoutingKey;

    @Value("${app.rabbitmq.queue.booking-cancelled}")
    private String bookingCancelledQueueName;

    @Value("${app.rabbitmq.routing.key.booking-cancelled}")
    private String bookingCancelledRoutingKey;

    @Value("${app.rabbitmq.queue.booking-result}")
    private String bookingResultQueueName;

    @Value("${app.rabbitmq.routing.key.booking-result}")
    private String bookingResultRoutingKey;

    @Bean
    public TopicExchange rideOfferExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue bookingQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding bookingQueueBinding(
            @Qualifier("bookingQueue") Queue bookingQueue,
            TopicExchange rideOfferExchange) {
        return BindingBuilder.bind(bookingQueue)
                .to(rideOfferExchange)
                .with(incomingRoutingKey);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(bookingCancelledQueueName).build();
    }

    @Bean
    public Binding bookingCancelledQueueBinding(
            @Qualifier("bookingCancelledQueue") Queue bookingCancelledQueue,
            TopicExchange rideOfferExchange) {
        return BindingBuilder.bind(bookingCancelledQueue)
                .to(rideOfferExchange)
                .with(bookingCancelledRoutingKey);
    }

    @Bean
    public Queue bookingResultQueue() {
        return QueueBuilder.durable(bookingResultQueueName).build();
    }

    @Bean
    public Binding bookingResultQueueBinding(
            @Qualifier("bookingResultQueue") Queue bookingResultQueue,
            TopicExchange rideOfferExchange) {
        return BindingBuilder.bind(bookingResultQueue)
                .to(rideOfferExchange)
                .with(bookingResultRoutingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

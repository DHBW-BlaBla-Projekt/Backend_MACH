package com.example.rideofferservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${app.rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing.key}")
    private String routingKey;

    @Value("${app.rabbitmq.queue.booking-cancelled}")
    private String bookingCancelledQueueName;

    @Value("${app.rabbitmq.routing.key.booking-cancelled}")
    private String bookingCancelledRoutingKey;

    @Value("${app.rabbitmq.queue.booking-request}")
    private String bookingRequestQueueName;

    @Value("${app.rabbitmq.routing.key.booking-result}")
    private String bookingResultRoutingKey;
    /**
     * Erstelle den Topic-Exchange für Ride-Offer-Events.
     */
    @Bean
    public TopicExchange rideOfferExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(bookingCancelledQueueName).build();
    }

    @Bean
    public Binding bookingCancelledBinding(
            Queue bookingCancelledQueue, TopicExchange rideOfferExchange) {
        return BindingBuilder.bind(bookingCancelledQueue)
                .to(rideOfferExchange)
                .with(bookingCancelledRoutingKey);
    }

    @Bean
    public Queue bookingRequestQueue() {
        return QueueBuilder.durable(bookingRequestQueueName).build();
    }

    @Bean
    public Binding bookingRequestBinding(
            Queue bookingRequestQueue, TopicExchange rideOfferExchange) {
        return BindingBuilder.bind(bookingRequestQueue)
                .to(rideOfferExchange)
                .with("booking.requested.routing");
    }

    /**
     * Konfiguriere RabbitTemplate mit JSON-Converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * Jackson2JsonMessageConverter Bean für die Serialisierung von Java-Objekten.
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

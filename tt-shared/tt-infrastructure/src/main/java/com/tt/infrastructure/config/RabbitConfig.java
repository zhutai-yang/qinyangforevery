package com.tt.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  @Bean
  public Queue extSyncQueue(@Value("${tt.mq.ext-sync-queue:tt.ext.sync}") String queueName) {
    return new Queue(queueName, true);
  }
}

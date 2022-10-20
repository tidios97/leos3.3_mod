package eu.europa.ec.leos.services.messaging.conf;

import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.ConnectionFactory;

import static org.springframework.jms.support.converter.MessageType.TEXT;

@Configuration
public class JmsServerConfig {

    @Bean(destroyMethod = "stop")
    public ActiveMQServer createBroker() throws Exception {
        EmbeddedActiveMQ server = new EmbeddedActiveMQ();
        server.setConfigResourcePath("broker_leos.xml");
        server.start();
        return server.getActiveMQServer();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQJMSConnectionFactory("vm://0");
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}

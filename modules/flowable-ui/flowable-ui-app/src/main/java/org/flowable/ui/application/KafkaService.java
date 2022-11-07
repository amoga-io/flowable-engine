package org.flowable.ui.application;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;

@Service
public class KafkaService {

    private static ProducerFactory<String, String> kafkaProducer = null;
    private static KafkaTemplate<String,String> kafkaTemplate = null;
    private static ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        List<String> servers = new ArrayList<>();
//        servers.add("20.244.13.46:9093");
//        servers.add("20.244.13.46:9092");
        servers.add(environmentMap.get("bootstrapServers"));
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,servers);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,1);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put("security.protocol",environmentMap.get("securityProtocol"));
        configProps.put("sasl.jaas.config",environmentMap.get("saslJaasConfig"));
        configProps.put("sasl.mechanism",environmentMap.get("saslMechanism"));
        configProps.put("client.dns.lookup",environmentMap.get("clientDnsLookup"));
        configProps.put("session.timeout.ms",environmentMap.get("session"));
        configProps.put("acks",environmentMap.get("acks"));
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private static ProducerFactory<String, String> getProducer() {
        if (kafkaProducer == null) {
            kafkaProducer = producerFactory();
        }
        return kafkaProducer;
    }
    public static KafkaTemplate<String, String> kafkaTemplate() {
        if(kafkaTemplate == null) {
            kafkaTemplate = new KafkaTemplate<>(getProducer());
        }
        return kafkaTemplate;
    }
}

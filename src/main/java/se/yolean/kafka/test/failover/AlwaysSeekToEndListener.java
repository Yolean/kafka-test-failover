package se.yolean.kafka.test.failover;

import java.util.Collection;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

public class AlwaysSeekToEndListener<K, V> implements ConsumerRebalanceListener {

    private Consumer<K, V> consumer;

    public AlwaysSeekToEndListener(Consumer<K, V> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        consumer.seekToEnd(partitions);
    }
}

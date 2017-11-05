# kafka-test-(failover|latency)

Actually renamed now to kafka-client-experience,
but repo names and [docker build](https://hub.docker.com/r/solsson/kafka-test-latency/builds/) may lag behind.

Just some kind of framework,
with some kind of [metrcis](https://prometheus.io/docs/concepts/metric_types/),
basically the sample
[Producer](https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html) and
[Consumer](https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html) behavior,
meant to run in [Kubernetes](https://github.com/Yolean/kubernetes-kafka)
using [test-kafka.yml](https://github.com/Yolean/kafka-test-failover/blob/master/test-kafka.yml).

Useful in two ways:
 * Keep it running to detect how Kafka ops affects clients.
 * Scale up and down, and/or change intervals, to see how it affects metrics.
   - see start of container logs for env var names.

Our current testing ambition is summarized in
https://github.com/Yolean/kubernetes-kafka/issues/84#issuecomment-341966523

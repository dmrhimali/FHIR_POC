package com.example.kafkatofhiringestion

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler





@SpringBootApplication
@EnableBinding(Sink::class)
class KafkaToFhirIngestionApplication {

    var logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    val fhirClient: FhirClient? = null

    @StreamListener(target = Sink.INPUT)
    fun consume(kafkaMessage:KafkaMessage) {

        val vpId = kafkaMessage.payload?.after?.id ?: kafkaMessage.payload?.before?.id

        logger.info("received a message  of type ${kafkaMessage.payload?.op} for member id: ${kafkaMessage.payload?.after?.id} ")

        when(kafkaMessage.payload?.op) {
            //read
            "c", "r" -> {
                val output = fhirClient?.createPatient(kafkaMessage.getPatient())
                logger.info("output create : $output ")
            }
            //update
            "u" -> {
                val patient = kafkaMessage.getPatient()
                val vpId = patient.identifier.firstOrNull()?.value
                val output = vpId?.let { fhirClient?.updatePatient(patient, it) }
                logger.info("output update : $output ")
            }
            //delete
            "d" -> {
                val patient = kafkaMessage.getPatient()
                val vpId = patient.identifier.firstOrNull()?.value
                println("vpId = $vpId")
                val output = vpId?.let { fhirClient?.deletePatient(it) }
                logger.info("output delete : $output ")
            }
        }

        logger.info("processing complete.")

    }

    //fix for error in processing delete records: 2021-10-15 14:33:57.941 ERROR 68774 --- [container-0-C-1] o.s.k.l.SeekToCurrentErrorHandler        : Backoff none exhausted for dbserver1.public.member-0@20
    //The default retry configuration is 3 attempts, 1 second initial delay, 2.0 multiplier, max delay 10 seconds.
    //
    //By default stateless retry is used, meaning that the retries are in memory.
    //
    //The aggregate delay for all retries for all records returned by a poll() must not exceed max.poll.interval.ms.
    //With modern versions of Spring for Apache Kafka (used by the binder); it is better to disable binder retries (maxAttempts=1) and use a SeekToCurrentErrorHandler with an appropriate BackOff configured.
    //
    //You can set the error handler with a ListenerContainerCustomizer<AbstractMessageListenerContainer<?, ?>> @Bean with return (container, dest, grp) -> container.setErrorHandler(handler).
    //To configure the container's error handler, add a ListenerContainerCustomizer<AbstractKafkaListenerContainerFactory> @Bean.
    //This avoids the problem mentioned above and only the max delay interval for one record must be less than max.poll.interval.ms.
    @Bean
    open fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String?, String?>): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String?, String?>?>? {
        //TODO: https://docs.spring.io/spring-kafka/docs/2.5.1.RELEASE/reference/html/#seek-to-current has implementation needed

        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        return factory
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(KafkaToFhirIngestionApplication::class.java, *args)
        }
    }
}
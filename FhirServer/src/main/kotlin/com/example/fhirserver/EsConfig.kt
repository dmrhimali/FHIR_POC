package com.example.fhirserver

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories


@Configuration
@EnableElasticsearchRepositories(basePackages = ["com.example.fhirserver.repository"])
class EsConfig {
    @Bean
    fun client(): RestHighLevelClient? {
        val clientConfiguration: ClientConfiguration = ClientConfiguration.builder()
            .connectedTo("localhost:9200")
            .build()
        return RestClients.create(clientConfiguration).rest()
    }

    @Bean
    fun elasticsearchTemplate(): ElasticsearchOperations? {
        return client()?.let { ElasticsearchRestTemplate(it) }
    }
}
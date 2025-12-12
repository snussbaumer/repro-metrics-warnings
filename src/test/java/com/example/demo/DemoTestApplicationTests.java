package com.example.demo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT, properties = {
        "spring.application.name=demo",
        "server.port=18080",
        "management.tracing.enabled=true",
        "management.prometheus.metrics.export.enabled=true",
        "management.endpoints.web.exposure.include=prometheus"
})
@WireMockTest(httpPort = 9004)
class DemoTestApplicationTests {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Test
    void repro() {
        WireMock.stubFor(get(urlPathEqualTo("/outside/test")) //
                .willReturn(aResponse() //
                        .withStatus(200) //
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //
                        .withBody("hello world")));

        RestTemplate restTemplate = restTemplateBuilder.build(); // replace by "new RestTemplate()" for test to pass
        String response = restTemplate.getForObject("http://localhost:18080/test", String.class);
        assertAll(
                () -> assertThat(response).isEqualTo("hello world"),
                () -> assertThat(logAppender.list)
                        .extracting(ILoggingEvent::getMessage)
                        .noneMatch(msg -> msg.contains("registration has failed")),
                () -> assertThat(restTemplate.getForObject("http://localhost:18080/actuator/prometheus", String.class))
                        .contains("http_client_requests_active_seconds_count{clientName=\"com.example.demo.TestFeignClient")
        );
    }

    // setup of log appender to capture logs

    private ListAppender<ILoggingEvent> logAppender;

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(io.micrometer.prometheusmetrics.PrometheusMeterRegistry.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
    }

}

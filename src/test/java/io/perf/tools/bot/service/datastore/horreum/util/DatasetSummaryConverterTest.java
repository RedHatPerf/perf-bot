package io.perf.tools.bot.service.datastore.horreum.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.hyperfoil.tools.horreum.api.data.View;
import io.hyperfoil.tools.horreum.api.services.DatasetService;
import io.perf.tools.bot.util.ResourceReader;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusComponentTest
class DatasetSummaryConverterTest {

    @Inject
    DatasetSummaryConverter converter;

    ObjectMapper objectMapper = new ObjectMapper();

    private View view;
    private DatasetService.DatasetSummary dataset;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        dataset = objectMapper.readValue(ResourceReader.getResourceAsStream("horreum/dataset.json"),
                DatasetService.DatasetSummary.class);
        view = objectMapper.readValue(ResourceReader.getResourceAsStream("horreum/view.json"), View.class);
    }

    @Test
    void serialize() {
        String expected = """
                ### (Default) View
                | Metric | Value |
                | ------ |:-----:|
                | PR Number | "" |
                | Commit SHA | "main" |
                | Max Response Time | 3768319 |
                | Mean Response Time | 792669 |
                | 99.99 Percentile | 3768319 |
                | 99.9 Percentile | 3162111 |
                
                """;
        Assertions.assertEquals(expected, converter.serialize(Pair.of(dataset, view)));
    }
}
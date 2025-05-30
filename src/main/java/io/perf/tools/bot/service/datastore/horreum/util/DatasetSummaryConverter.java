package io.perf.tools.bot.service.datastore.horreum.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.hyperfoil.tools.horreum.api.data.IndexedLabelValueMap;
import io.hyperfoil.tools.horreum.api.data.View;
import io.hyperfoil.tools.horreum.api.services.DatasetService;
import io.perf.tools.bot.util.Converter;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

@ApplicationScoped
public class DatasetSummaryConverter implements Converter<Pair<DatasetService.DatasetSummary, View>> {
    @Override
    public String serialize(Pair<DatasetService.DatasetSummary, View> value) {
        StringBuilder builder = new StringBuilder();
        IndexedLabelValueMap values = value.getKey().view;

        builder.append("### (").append(value.getValue().name).append(") View").append("\n");
        builder.append("| Metric | Value |").append("\n");
        builder.append("| ------ |:-----:|").append("\n");

        value.getValue().components.forEach(component -> {
            Optional<JsonNode> componentValue = values.get(Integer.toString(component.id)).values().stream().findFirst();
            builder.append("| ").append(component.headerName).append(" | ").append(componentValue.orElse(null))
                    .append(" |\n");
        });

        builder.append("\n");

        return builder.toString();
    }
}

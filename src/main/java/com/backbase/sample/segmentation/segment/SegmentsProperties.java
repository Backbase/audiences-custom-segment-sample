package com.backbase.sample.segmentation.segment;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
    prefix = SegmentsProperties.PREFIX,
    ignoreUnknownFields = false,
    ignoreInvalidFields = false
)
public class SegmentsProperties {

    public static final String PREFIX = "backbase.custom-segment-sample.segments";

    private List<SegmentDefinition> definitions;

    public List<SegmentDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<SegmentDefinition> definitions) {
        this.definitions = definitions;
    }

}

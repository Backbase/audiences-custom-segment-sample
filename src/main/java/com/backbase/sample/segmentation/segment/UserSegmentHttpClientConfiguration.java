package com.backbase.sample.segmentation.segment;

import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER;

import com.backbase.buildingblocks.communication.client.ApiClientConfig;
import com.backbase.openapi.usersegment.ApiClient;
import com.backbase.openapi.usersegment.v1.SegmentServiceApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("backbase.communication.services.user-segment")
public class UserSegmentHttpClientConfiguration extends ApiClientConfig {

    public static final String USER_SEGMENT_SERVICE_ID = "user-segment";

    public UserSegmentHttpClientConfiguration() {
        super(USER_SEGMENT_SERVICE_ID);
    }

    @Bean("user-segment#service-api#SegmentServiceApi")
    public SegmentServiceApi segmentServiceApi() {
        return new SegmentServiceApi(createApiClient());
    }

    private ApiClient createApiClient() {
        return new ApiClient(getRestTemplate())
            .setBasePath(createBasePath())
            .addDefaultHeader(INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
    }

}

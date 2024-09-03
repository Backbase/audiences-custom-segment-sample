package com.backbase.sample.segmentation.recommendation.calculation;


import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER;

import com.backbase.buildingblocks.communication.client.ApiClientConfig;
import com.backbase.openapi.usermanager.ApiClient;
import com.backbase.openapi.usermanager.v2.UserManagementApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("backbase.communication.services.user-manager")
public class UserManagerHttpClientConfiguration extends ApiClientConfig {

    public static final String SERVICE_ID = "user-manager";

    public UserManagerHttpClientConfiguration() {
        super(SERVICE_ID);
    }

    @Bean("user-manager#service-api#UserManagementApi")
    public UserManagementApi userManagementApi() {
        return new UserManagementApi(createApiClient());
    }

    private ApiClient createApiClient() {
        return new ApiClient(getRestTemplate())
            .setBasePath(createBasePath())
            .addDefaultHeader(INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
    }

}

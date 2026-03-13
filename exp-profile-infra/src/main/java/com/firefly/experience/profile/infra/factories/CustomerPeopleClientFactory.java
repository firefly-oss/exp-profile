package com.firefly.experience.profile.infra.factories;

import com.firefly.domain.people.sdk.api.CustomersApi;
import com.firefly.domain.people.sdk.invoker.ApiClient;
import com.firefly.experience.profile.infra.properties.CustomerPeopleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that wires SDK client beans for the {@code domain-customer-people} service.
 * <p>
 * {@code CustomersApi} consolidates all customer profile operations exposed by the domain
 * service, including personal data, contact data (email/phone), addresses, identity
 * documents, and consents — they are all grouped under the "customers" OpenAPI tag.
 * <p>
 * Base path is configured via
 * {@code api-configuration.domain-platform.customer-people.base-path} in
 * {@code application.yaml}.
 */
@Component
@RequiredArgsConstructor
public class CustomerPeopleClientFactory {

    private final CustomerPeopleProperties properties;

    private ApiClient buildApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(properties.getBasePath());
        return client;
    }

    /**
     * Provides the {@link CustomersApi} bean covering personal data, contact data,
     * addresses, identity documents, and consent operations.
     *
     * @return configured {@link CustomersApi} instance
     */
    @Bean
    public CustomersApi customersApi() {
        return new CustomersApi(buildApiClient());
    }
}

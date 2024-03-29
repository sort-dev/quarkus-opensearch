package dev.sort.oss.quarkus.opensearch.client.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.HealthStatus;

@Readiness
@ApplicationScoped
public class OpenSearchHealthCheck implements HealthCheck {
    @Inject
    OpenSearchClient osClient;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("OpenSearch cluster health check").up();
        try {
            var status = osClient.cluster().health().status();

            if (status == HealthStatus.Red) {
                builder.down().withData("status", status.name());
            } else {
                builder.up().withData("status", status.name());
            }
        } catch (Exception e) {
            return builder.down().withData("reason", e.getMessage()).build();
        }
        return builder.build();
    }
}

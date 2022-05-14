package dev.sort.oss.quarkus.opensearch.client.deployment;

import dev.sort.oss.quarkus.opensearch.client.runtime.OpenSearchClientProducer;
import dev.sort.oss.quarkus.opensearch.client.runtime.OpenSearchHealthCheck;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

class OpenSearchClientProcessor {

    static final String FEATURE = "opensearch-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem build() {
        return AdditionalBeanBuildItem.unremovableOf(OpenSearchClientProducer.class);
    }

    @BuildStep
    HealthBuildItem addHealthCheck(OpenSearchBuildTimeConfig buildTimeConfig) {
        return new HealthBuildItem(OpenSearchHealthCheck.class.getCanonicalName(),
                buildTimeConfig.healthEnabled);
    }

    @BuildStep
    DevServicesOpenSearchBuildItem devServices() {
        return new DevServicesOpenSearchBuildItem("quarkus.opensearch.host");
    }
}

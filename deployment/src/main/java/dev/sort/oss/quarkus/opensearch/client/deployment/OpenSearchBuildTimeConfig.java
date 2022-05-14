package dev.sort.oss.quarkus.opensearch.client.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "opensearch", phase = ConfigPhase.BUILD_TIME)
public class OpenSearchBuildTimeConfig {
    /**
     * Whether or not an health check is published in case the smallrye-health extension is present.
     */
    @ConfigItem(name = "health.enabled", defaultValue = "true")
    public boolean healthEnabled;
}

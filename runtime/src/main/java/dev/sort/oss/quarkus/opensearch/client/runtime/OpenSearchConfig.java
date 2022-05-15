package dev.sort.oss.quarkus.opensearch.client.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

@ConfigRoot(name = "opensearch", phase = ConfigPhase.RUN_TIME)
public class OpenSearchConfig {

    /**
     * The main host for OpenSearch server
     */
    @ConfigItem(defaultValue = "http://localhost:9200")
    public String host;

    /**
     * The username for basic HTTP authentication.
     */
    @ConfigItem
    public Optional<String> username;

    /**
     * The password for basic HTTP authentication.
     */
    @ConfigItem
    public Optional<String> password;

}

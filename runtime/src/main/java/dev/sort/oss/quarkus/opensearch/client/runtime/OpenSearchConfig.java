package dev.sort.oss.quarkus.opensearch.client.runtime;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public class OpenSearchConfig {

    /**
     * The list of hosts of the Elasticsearch servers.
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

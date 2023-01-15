package dev.sort.oss.quarkus.opensearch.client.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Objects;
import java.util.Optional;

@ConfigRoot(name = "opensearch.devservices", phase = ConfigPhase.BUILD_TIME)
public class OpenSearchDevServicesBuildTimeConfig {

    /**
     * If Dev Services for OpenSearch has been explicitly enabled or disabled. Dev Services are generally enabled
     * by default, unless there is an existing configuration present. For OpenSearch, Dev Services starts a server unless
     * {@code quarkus.opensearch.host} is set.
     */
    @ConfigItem
    public Optional<Boolean> enabled = Optional.empty();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem
    public Optional<Integer> port;

    /**
     * The container image to use.
     * Defaults to the OpenSearch image provided by the OpenSearch project.
     */
    @ConfigItem(defaultValue = "opensearchproject/opensearch:2.3.0")
    public String imageName;

    /**
     * The value for the ES_JAVA_OPTS env variable.
     * Defaults to setting the heap to 1GB.
     */
    @ConfigItem(defaultValue = "-Xmx1g")
    public String javaOpts;

    /**
     * Indicates if the OpenSearch server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for OpenSearch starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-opensearch} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @ConfigItem(defaultValue = "true")
    public boolean shared;

    /**
     * The value of the {@code quarkus-dev-service-opensearch} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for OpenSearch looks for a container with the
     * {@code quarkus-dev-service-opensearch} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-opensearch} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared OpenSearch servers.
     */
    @ConfigItem(defaultValue = "opensearch")
    public String serviceName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenSearchDevServicesBuildTimeConfig that = (OpenSearchDevServicesBuildTimeConfig) o;
        return shared == that.shared
                && enabled.equals(that.enabled)
                && port.equals(that.port)
                && imageName.equals(that.imageName)
                && javaOpts.equals(that.javaOpts)
                && serviceName.equals(that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, port, imageName, javaOpts, shared, serviceName);
    }
}

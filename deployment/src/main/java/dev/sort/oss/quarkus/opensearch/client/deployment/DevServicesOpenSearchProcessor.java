package dev.sort.oss.quarkus.opensearch.client.deployment;


import io.quarkus.builder.BuildException;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;
import org.jboss.logging.Logger;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * Starts an Elasticsearch server as dev service if needed.
 */
public class DevServicesOpenSearchProcessor {
    private static final Logger log = Logger.getLogger(DevServicesOpenSearchProcessor.class);

    /**
     * Label to add to shared Dev Service for OpenSearch running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-opensearch";
    static final int OPENSEARCH_PORT = 9200;

    private static final ContainerLocator openSearchContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            OPENSEARCH_PORT);

    static volatile DevServicesResultBuildItem.RunningDevService devService;
    static volatile OpenSearchDevServicesBuildTimeConfig cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem startOpenSearchDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            OpenSearchDevServicesBuildTimeConfig configuration,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig,
            List<DevServicesOpenSearchBuildItem> devservicesOpenSearchBuildItems) throws BuildException {

        if (devservicesOpenSearchBuildItems.isEmpty()) {
            // safety belt in case a module depends on this one without producing the build item
            return null;
        }

        DevServicesOpenSearchBuildItemsConfiguration buildItemsConfig = new DevServicesOpenSearchBuildItemsConfiguration(
                devservicesOpenSearchBuildItems);

        if (devService != null) {
            boolean shouldShutdownTheServer = !configuration.equals(cfg);
            if (!shouldShutdownTheServer) {
                return devService.toBuildItem();
            }
            shutdownOpenSearch();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "OpenSearch Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startOpenSearch(dockerStatusBuildItem, configuration, buildItemsConfig, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdownOpenSearch();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {
            log.infof(
                    "Dev Services for OpenSearch started. Other Quarkus applications in dev mode will find the "
                            + "server automatically. For Quarkus applications in production mode, you can connect to"
                            + " this by configuring your application to use %s",
                    getOpenSearchHosts(buildItemsConfig));
        }
        return devService.toBuildItem();
    }

    public static String getOpenSearchHosts(DevServicesOpenSearchBuildItemsConfiguration buildItemsConfiguration) {
        String hostsConfigProperty = buildItemsConfiguration.hostsConfigProperties.stream().findAny().get();
        return devService.getConfig().get(hostsConfigProperty);
    }

    private void shutdownOpenSearch() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the OpenSearch server", e);
            } finally {
                devService = null;
            }
        }
    }

    private DevServicesResultBuildItem.RunningDevService startOpenSearch(
            DockerStatusBuildItem dockerStatusBuildItem,
            OpenSearchDevServicesBuildTimeConfig config,
            DevServicesOpenSearchBuildItemsConfiguration buildItemConfig,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) throws BuildException {
        if (!config.enabled.orElse(true)) {
            // explicitly disabled
            log.debug("Not starting dev services for OpenSearch, as it has been disabled in the config.");
            return null;
        }

        for (String hostsConfigProperty : buildItemConfig.hostsConfigProperties) {
            // Check if OpenSearh host property is set
            if (ConfigUtils.isPropertyPresent(hostsConfigProperty)) {
                log.debugf("Not starting dev services for OpenSearch, the %s property is configured.", hostsConfigProperty);
                return null;
            }
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warnf("Docker isn't working, please configure the OpenSearch hosts property (%s).",
                    displayProperties(buildItemConfig.hostsConfigProperties));
            return null;
        }

        // Hibernate search Elasticsearch have a version configuration property, we need to check that it is coherent
        // with the image we are about to launch
        if (buildItemConfig.version != null) {
            String containerTag = config.imageName.substring(config.imageName.indexOf(':') + 1);
            if (!containerTag.startsWith(buildItemConfig.version)) {
                throw new BuildException(
                        "Dev services for OpenSearch detected a version mismatch, container image is " + config.imageName
                                + " but the configured version is " + buildItemConfig.version +
                                ". Either configure a different image or disable dev services for OpenSearch.",
                        Collections.emptyList());
            }
        }

        final Optional<ContainerAddress> maybeContainerAddress = openSearchContainerLocator.locateContainer(
                config.serviceName,
                config.shared,
                launchMode.getLaunchMode());

        // Starting the server
        final Supplier<DevServicesResultBuildItem.RunningDevService> defaultOpenSearchSupplier = () -> {
            var container = new OpensearchContainer(DockerImageName.parse(config.imageName));

            // official dev container sets this internally and cannot be set here
            // ConfigureUtil.configureSharedNetwork(container, "opensearch");

            if (config.serviceName != null) {
                container.withLabel(DEV_SERVICE_LABEL, config.serviceName);
            }
            if (config.port.isPresent()) {
                container.setPortBindings(List.of(config.port.get() + ":" + OPENSEARCH_PORT));
            }

            timeout.ifPresent(container::withStartupTimeout);
            container.addEnv("OPENSEARCH_JAVA_OPTS", config.javaOpts);
            container.addEnv("discovery.type", "single-node");
            container.addEnv("DISABLE_INSTALL_DEMO_CONFIG", "true");
            // container.addEnv("DISABLE_SECURITY_PLUGIN", "true");

            container.setWaitStrategy((new HttpWaitStrategy())
                    .forPort(config.port.orElse(OPENSEARCH_PORT))
                    .forStatusCodeMatching(response -> response == 200 || response == 401));

            container.start();

            var addr = container.getHttpHostAddress(); // container.getHost() + ":" + container.getMappedPort(OPENSEARCH_PORT);

            return new DevServicesResultBuildItem.RunningDevService(OpenSearchClientProcessor.FEATURE,
                    container.getContainerId(),
                    container::close,
                    buildPropertiesMap(buildItemConfig, addr));
        };

        return maybeContainerAddress
                .map(containerAddress -> new DevServicesResultBuildItem.RunningDevService(
                        OpenSearchClientProcessor.FEATURE,
                        containerAddress.getId(),
                        null,
                        buildPropertiesMap(buildItemConfig, containerAddress.getUrl())))
                .orElseGet(defaultOpenSearchSupplier);
    }

    private Map<String, String> buildPropertiesMap(DevServicesOpenSearchBuildItemsConfiguration buildItemConfig,
                                                   String httpHosts) {
        Map<String, String> propertiesToSet = new HashMap<>();
        for (String property : buildItemConfig.hostsConfigProperties) {
            propertiesToSet.put(property, httpHosts);
        }
        return propertiesToSet;
    }

    private String displayProperties(Set<String> hostsConfigProperties) {
        return String.join(" and ", hostsConfigProperties);
    }

    private static class DevServicesOpenSearchBuildItemsConfiguration {
        private Set<String> hostsConfigProperties;
        private String version;

        private DevServicesOpenSearchBuildItemsConfiguration(List<DevServicesOpenSearchBuildItem> buildItems)
                throws BuildException {
            hostsConfigProperties = new HashSet<>(buildItems.size());

            // check that all build items agree on the version and distribution to start
            for (DevServicesOpenSearchBuildItem buildItem : buildItems) {
                if (version == null) {
                    version = buildItem.getVersion();
                } else if (!version.equals(buildItem.getVersion())) {
                    // safety guard but should never occur as only Hibernate Search ORM Elasticsearch configure the version
                    throw new BuildException("Multiple extensions request Elasticsearch Dev Services on different version.",
                            Collections.emptyList());
                }

                hostsConfigProperties.add(buildItem.getHostsConfigProperty());
            }
        }
    }
}

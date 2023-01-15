# Quarkus OpenSearch Client

An extension to Quarkus providing an OpenSearch Java client, as well as Dev Services container during testing.

Primary version 0.3.x, using:

* Quarkus: `2.14.0` (0.3.0) or `2.15.0` (0.3.1)
* OpenSearch Java Client: `2.1.0`
* OpenSearch Server: `2.3.0` (via Docker in Dev Services)
* Kotlin: `1.7.21`
* OpenSearch-Project dev container: `2.0.0`

For experimental version 0.5.x, using:

* Quarkus: `3.0.0.Alpha2`
* OpenSearch Java Client: `2.1.0`
* OpenSearch Server: `2.4.1`  (via Docker in Dev Services)
* Kotlin: `1.8.0`
* OpenSearch-Project dev container: `2.0.0`

This is by far not fully configurable and is just a base implementation based off of the code from Quarkus Elasticsearch
extension.

Releases are available from JitPack: https://jitpack.io/#sort-dev/quarkus-opensearch-client

Latest release dependencies...

for Quarkus 2.14.0 is:

```text
com.github.sort-dev:quarkus-opensearch:0.3.0
```

or for Quarkus 2.15.0 is:

```text
com.github.sort-dev:quarkus-opensearch:0.3.2
```

or for experimental Quarkus 3.0.0-Alpha2 version:

```text
com.github.sort-dev:quarkus-opensearch:0.5.5
```

Can inject either `OpenSearchClient` or `OpenSearchAsyncClient`

!! Work in Progress !!
# Quarkus OpenSearch Client

An extension to Quarkus providing an OpenSearch Java client, as well as Dev Services container during testing.

Using:

Quarkus: `3.0.0.Alpha1`
OpenSearch Java Client: `2.1.0`
OpenSearch Server: `2.3.0`  (via Docker in Dev Services)
Kotlin: `1.7.21`
OpenSearch-Project dev container: `2.0.0`

This is by far not fully configurable and is just a base implementation based off of the code from Quarkus Elasticsearch
extension.

Releases are available from JitPack: https://jitpack.io/#sort-dev/quarkus-opensearch-client

Latest release dependency is:

```text
com.github.sort-dev:quarkus-opensearch:0.5.0
```

(for version 2.14.x of Quarkus, use version 0.3.x of this library)

Can inject either `OpenSearchClient` or `OpenSearchAsyncClient`

!! Work in Progress !!
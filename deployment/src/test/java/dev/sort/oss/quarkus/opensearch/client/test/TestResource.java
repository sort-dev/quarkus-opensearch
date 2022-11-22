package dev.sort.oss.quarkus.opensearch.client.test;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.search.Hit;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Path("/fruits")
public class TestResource {
    @Inject
    OpenSearchClient osClient;

    @Inject
    OpenSearchAsyncClient osAsyncClient;

    @POST
    public void index(Fruit fruit) throws IOException {
        osClient.index(i -> i.index("fruits").id(fruit.id).refresh(Refresh.True).document(fruit));
    }

    @GET
    @Path("/search")
    public List<Fruit> search(@QueryParam("term") String term, @QueryParam("match") String match) throws IOException {
        var response =
                osClient.search(b -> b.index("fruits").query(q -> q.matchAll(ma -> ma)), Fruit.class);

        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    @GET
    @Path("/search_async")
    public CompletionStage<List<Fruit>> searchAsync(@QueryParam("term") String term, @QueryParam("match") String match) throws IOException {
        return osAsyncClient.search(b -> b.index("fruits").query(q -> q.matchAll(ma -> ma)), Fruit.class)
                .thenApply(response ->
                        response.hits().hits().stream().map(Hit::source).collect(Collectors.toList())
                );
    }

    public static class Fruit {
        public String id;
        public String name;
        public String color;
    }
}

package dev.sort.oss.quarkus.opensearch.client.test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;

@Path("/fruits")
public class TestResource {
    @Inject
    OpenSearchClient osClient;

    @POST
    public void index(Fruit fruit) throws IOException {
        osClient.index(new IndexRequest.Builder<Fruit>().index("fruits").id(fruit.id).refresh(Refresh.True).document(fruit).build());
    }

    @GET
    @Path("/search")
    public List<Fruit> search(@QueryParam("term") String term, @QueryParam("match") String match) throws IOException {
        var response = osClient.search(new SearchRequest.Builder().index("fruits").query(QueryBuilders.matchAll().build()._toQuery()).build(), Fruit.class);
        return response.hits().hits().stream().map(h -> h.source()).collect(Collectors.toList());
    }

    public static class Fruit {
        public String id;
        public String name;
        public String color;
    }
}

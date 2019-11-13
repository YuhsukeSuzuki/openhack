package com.bfyoc.team4;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetRating {
    @FunctionName("GetRating")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "rating/{id}")
                HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(
                name = "cosmosDB",
                databaseName = "team4db",
                collectionName = "team4container",
                sqlQuery = "select c.id, c.userId, c.productId, c.timestamp, "
                         + "c.locationName, c.rating, c.userNotes from team4container c "
                         + "where c.id={id}",
                connectionStringSetting = "AzureCosmosDBConnection")
                Rating[] ratings,
            final ExecutionContext context) {
        if (Optional.of(ratings).isPresent() && ratings.length == 1) {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(ratings[0])
                    .build();
        } else {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("{\"error\":\"Not found\"}")
                            .build();
        }
    }
}

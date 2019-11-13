package com.bfyoc.team4;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;


/**
 * Azure Functions with HTTP Trigger.
 */
public class GetRatings {

    @FunctionName("GetRatings")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route="ratings/{userId}") HttpRequestMessage<Optional<String>> request,
            @CosmosDBInput(name = "cosmosDB",
                    databaseName = "team4db",
                    collectionName = "team4container",
                    sqlQuery = "select c.id, c.userId, c.productId, c.timestamp, c.locationName, c.rating, c.userNotes from c where c.userId = {userId}",
                    connectionStringSetting="AzureCosmosDBConnection")
                    Rating[] ratings,
            final ExecutionContext context) {

                context.getLogger().info("Parameters are: " + request.getQueryParameters());
                context.getLogger().info("Items[0] from the database are " + ratings[0]);
        
                // Convert and display
        if (Optional.of(ratings).isPresent()){
            return request.createResponseBuilder(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body(ratings)
            .build();
        }
        else{
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
            .body("{\"error\":\"Not found\"}")
            .build();
        }
    }
}

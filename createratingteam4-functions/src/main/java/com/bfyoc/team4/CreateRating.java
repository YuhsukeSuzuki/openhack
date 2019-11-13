package com.bfyoc.team4;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;


/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateRating {

    @FunctionName("CreateRating")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route="rating") HttpRequestMessage<Optional<Rating>> request,
            @CosmosDBOutput(name = "cosmosDB",
                    databaseName = "team4db",
                    collectionName = "team4container",
                    connectionStringSetting="AzureCosmosDBConnection") OutputBinding<Rating> rating,
            final ExecutionContext context) {
        context.getLogger().info("CreateRating function is invoked.");

        Rating _rating = request.getBody().get();
        // guid
        _rating.setId(UUID.randomUUID().toString());
        // timestamp
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        _rating.setTimestamp(zonedDateTime.format(dtf));

        // Insert data to Cosmos DB
        rating.setValue(_rating);

        return request.createResponseBuilder(HttpStatus.CREATED)
                .header("content-type","application/json")
                .body(_rating)
                .build();
    }
}

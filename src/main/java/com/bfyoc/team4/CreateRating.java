package com.bfyoc.team4;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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
                    route = "rating") HttpRequestMessage<Optional<Rating>> request,
            @CosmosDBOutput(name = "cosmosDB",
                    databaseName = "team4db",
                    collectionName = "team4container",
                    connectionStringSetting = "AzureCosmosDBConnection") OutputBinding<Rating> rating,
            final ExecutionContext context) {
        context.getLogger().info("CreateRating function is invoked.");

        Rating _rating = request.getBody().get();
        String errorMessage = null;
        try {
            if ( invokeValidationAPI("https://serverlessohuser.trafficmanager.net",
                    "/api/GetUser",
                    "userId",
                    _rating.getUserId())
            && invokeValidationAPI("https://serverlessohproduct.trafficmanager.net",
                    "/api/GetProduct",
                    "productId",
                    _rating.getProductId())
            && (_rating.getRating() > -1 && _rating.getRating() < 6 )) {

                // add elements to rating
                // guid
                _rating.setId(UUID.randomUUID().toString());
                // timestamp
                ZonedDateTime zonedDateTime = ZonedDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                _rating.setTimestamp(zonedDateTime.format(dtf));

                // Insert data to Cosmos DB
                rating.setValue(_rating);
            }
            else {
                errorMessage = "Invalid attribute is found.";
            }
        } catch (Exception e) {
            errorMessage = "Invalid attribute is found.";
        }

        if(Objects.isNull(errorMessage)) {
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("content-type", "application/json")
                    .body(_rating)
                    .build();
        }
        else {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("content-type", "application/json")
                    .body("{\"error\":\"" + errorMessage + "\"}")
                    .build();
        }
    }

    Boolean invokeValidationAPI(String _targetURI, String _path, String _queryParam, String queryValue) {
        Response response = ClientBuilder.newClient()
                .target(_targetURI)
                .path(_path)
                .queryParam(_queryParam, queryValue)
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            return true;
        } else {
            return false;
        }
    }
}

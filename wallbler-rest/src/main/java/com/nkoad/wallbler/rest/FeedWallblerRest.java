package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.exception.AccountAlreadyExistsException;
import com.nkoad.wallbler.service.OsgiConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "Feed Wallbler Rest Service", service = FeedWallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class FeedWallblerRest {
    @Reference
    private OsgiConfigurationService osgiService;

    // Get feed factories
    @Path("/feed/factories")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<String> getFactories() {
        return osgiService.getWallblerFeedFactories();
    }

    // Get feed list
    @Path("/feed")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<Map<String, Object>> getFeeds() {
        return osgiService.readFeeds();
    }

    // Create a new feed
    @Path("/feed")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Response createFeed(HashMap<String, Object> config) {
        try {
            return status(201).entity(osgiService.create(config)).build();
        } catch (AccountAlreadyExistsException e) {
            return status(409).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
    }

    private String generateErrorMessage(String message) {
        return String.format("{\"message\":\"%s\"}", message);
    }

}

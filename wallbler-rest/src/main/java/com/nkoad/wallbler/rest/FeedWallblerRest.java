package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.exception.AccountAlreadyExistsException;
import com.nkoad.wallbler.exception.ConfigNotFoundException;
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
import java.util.stream.Collectors;

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
        return osgiService.getWallblerFeedFactories().collect(Collectors.toList());
    }

    // Get feed list
    @Path("/feed")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<Map<String, Object>> getFeeds() {
        return osgiService.readFeeds().collect(Collectors.toList());
    }

    // Get a single feed
    @Path("/feed/{feed_pid:([^:]*[^/]$)}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getFeed(@PathParam("feed_pid") String feedPid) {
        try {
            Map<String, Object> config = osgiService.read(feedPid);
            return status(200).entity(config).build();
        } catch (ConfigNotFoundException e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
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

    // Update a feed
    @Path("/feed/{feed_pid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response updateFeed(@PathParam("feed_pid") String feedPid, HashMap<String, Object> config) {
        try {
            return status(200).entity(osgiService.update(feedPid, config)).build();
        } catch (ConfigNotFoundException e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
    }

    // Delete a feed
    @Path("/feed/{feed_pid}")
    @DELETE
    public Response deleteFeed(@PathParam("feed_pid") String feedPid) {
        try {
            osgiService.delete(feedPid);
            return status(204).build();
        } catch (ConfigNotFoundException e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
    }

    private String generateErrorMessage(String message) {
        return String.format("{\"message\":\"%s\"}", message);
    }

}

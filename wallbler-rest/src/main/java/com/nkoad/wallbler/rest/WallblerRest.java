package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import org.json.JSONArray;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "WallblerRestService", service = WallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class WallblerRest {
    @Reference
    private Cache cache;

    // Get all posts
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getAllData(@QueryParam("socials") String socials,
                            @QueryParam("limit") Integer limit) {
        JSONArray data = cache.getAllData(socials, limit);
        return status(200).entity(data.toString()).build();
    }

    // Get accepted posts only
    @Path("/accepted")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getAcceptedData(@QueryParam("socials") String socials,
                            @QueryParam("limit") Integer limit) {
        JSONArray data = cache.getAcceptedData(socials, limit);
        return status(200).entity(data.toString()).build();
    }

    // Get non accepted posts only
    @Path("/nonAccepted")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getNonAcceptedData(@QueryParam("socials") String socials,
                            @QueryParam("limit") Integer limit) {
        JSONArray data = cache.getNonAcceptedData(socials, limit);
        return status(200).entity(data.toString()).build();
    }

    // Set 'accept' to posts by 'socialMediaType', 'socialId' and 'accepted' fields
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PATCH
    public Response setAccept(List<WallblerItem> wallblerItems) {
        cache.setAccept(wallblerItems);
        return status(200).build();
    }

    // Delete posts by feed name
    @Path("/{social_media_type}/{feed_name}")
    @DELETE
    public Response deletePostsByFeedName(@PathParam("social_media_type") String socialMediaType,
                                          @PathParam("feed_name") String feedName) {
        cache.deletePostsByFeedName(socialMediaType, feedName);
        return status(200).build();
    }

}

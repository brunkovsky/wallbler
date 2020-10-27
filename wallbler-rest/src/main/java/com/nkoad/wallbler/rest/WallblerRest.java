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
import java.util.Set;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "WallblerRestService", service = WallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class WallblerRest {
    @Reference
    private Cache cache;

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getData(@QueryParam("socials") String socials,
                            @QueryParam("limit") Integer limit) {
        JSONArray data = cache.getData(socials, limit);
        return status(200).entity(data.toString()).build();
    }

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

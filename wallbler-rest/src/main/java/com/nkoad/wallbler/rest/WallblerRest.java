package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.cache.definition.Cache;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "Wallbler Rest Service", service = WallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class WallblerRest {
    @Reference
    private Cache cache;

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getData(@QueryParam("socials") String socials,
                            @QueryParam("accepted") Boolean accepted) {
        return status(200).entity(cache.getData(socials, accepted)).build();
    }

    @Path("/{social_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PATCH
    public Response setAccept(@PathParam("social_id") Integer socialId, Boolean accept) {
        cache.setAccept(socialId, accept);
        return status(200).build();
    }

}

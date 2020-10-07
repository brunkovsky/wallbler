package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.cache.definition.Cache;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "Wallbler Rest Service", service = WallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class WallblerRest {
    @Reference
    private Cache cache;
    @Path("/")
    @Produces("application/json")
    @GET
    public Response getData(@QueryParam("socials") String socials,
                            @QueryParam("accepted") Boolean accepted) {
        return status(200).entity(cache.getData(socials, accepted)).build();
    }

}

package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

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
        JSONArray data = cache.getData(socials, accepted);
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

}

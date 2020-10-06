package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.service.OsgiConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/wallbler")
@Component(name = "Feed Wallbler Rest Service", service = FeedWallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class FeedWallblerRest {
    @Reference
    private OsgiConfigurationService osgiService;

    // Get feed factories
    @Path("/feed/factory_pids")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<String> getFeedFactoryPids() {
        return osgiService.getWallblerFeedFactories();
    }

    // Get feed list
    @Path("/feed")
    @Produces("application/json")
    @GET
    public List<Map<String, Object>> getFeeds() {
        return osgiService.readFeeds();
    }

}

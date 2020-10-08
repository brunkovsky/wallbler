package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.service.OsgiConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "Account Wallbler Rest Service", service = AccountWallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class AccountWallblerRest {
    @Reference
    private OsgiConfigurationService osgiService;

    // Get account factories
    @Path("/account/factories")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<String> getFactories() {
        return osgiService.getWallblerAccountFactories();
    }

    // Get account list
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<Map<String, Object>> getAccounts() {
        return osgiService.readAccounts();
    }

    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Response createAccount(HashMap<String, Object> hashMap) {
        return status(200).entity(osgiService.create(hashMap)).build();
    }

}

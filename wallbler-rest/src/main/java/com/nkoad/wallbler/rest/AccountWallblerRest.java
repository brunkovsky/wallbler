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

}

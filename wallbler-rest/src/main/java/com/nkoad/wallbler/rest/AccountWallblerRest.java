package com.nkoad.wallbler.rest;

import com.nkoad.wallbler.service.OSGiConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.status;

@Path("/wallbler")
@Component(name = "AccountWallblerRestService", service = AccountWallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class AccountWallblerRest {
    @Reference
    private OSGiConfigurationService osgiService;

    // Get account factories
    @Path("/account/factories")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<String> getFactories() {
        return osgiService.getWallblerAccountFactories().collect(Collectors.toList());
    }

    // Get account list
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public List<Map<String, Object>> getAccounts() {
        return osgiService.readAccounts().collect(Collectors.toList());
    }

    // Get a single account
    @Path("/account/{account_pid:([^:]*[^/]$)}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getAccount(@PathParam("account_pid") String accountPid) {
        try {
            Map<String, Object> config = osgiService.read(accountPid);
            return status(200).entity(config).build();
        } catch (Exception e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        }
    }

    // Get all feeds from the account
    @Path("/account/{account_pid:[^:]*/$}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getFeedsFromAccount(@PathParam("account_pid") String accountPid) {
        try {
            List<Map<String, Object>> feeds = osgiService.getFeedsFromAccount(accountPid.substring(0, accountPid.length() - 1)).collect(Collectors.toList());
            return status(200).entity(feeds).build();
        } catch (Exception e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        }
    }

    // Create a new account
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Response createAccount(Map<String, Object> config) {
        try {
            return status(201).entity(osgiService.create(config)).build();
        } catch (Exception e) {
            return status(409).entity(generateErrorMessage(e.getMessage())).build();
        }
    }

    // Update an account
    @Path("/account/{account_pid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response updateAccount(@PathParam("account_pid") String accountPid, Map<String, Object> config) {
        try {
            return status(200).entity(osgiService.update(accountPid, config)).build();
        } catch (Exception e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        }
    }

    // Delete an account
    @Path("/account/{account_pid}")
    @DELETE
    public Response deleteAccount(@PathParam("account_pid") String accountPid) {
        try {
            osgiService.delete(accountPid);
            return status(204).build();
        } catch (Exception e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        }
    }

    private String generateErrorMessage(String message) {
        return String.format("{\"message\":\"%s\"}", message);
    }

}

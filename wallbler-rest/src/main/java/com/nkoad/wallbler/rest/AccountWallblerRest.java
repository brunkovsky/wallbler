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

    // Get a single account
    @Path("/account/{account_pid:([^:]*[^/]$|$)}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getAccount(@PathParam("account_pid") String accountPid) {
        try {
            Map<String, Object> config = osgiService.read(accountPid);
            return status(200).entity(config).build();
        } catch (ConfigNotFoundException e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
    }

    // Create a new account
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Response createAccount(HashMap<String, Object> config) {
        try {
            return status(201).entity(osgiService.create(config)).build();
        } catch (AccountAlreadyExistsException e) {
            return status(409).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
    }

    // Update an account
    @Path("/account/{account_pid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response updateAccount(@PathParam("account_pid") String accountPid, HashMap<String, Object> config) {
        try {
            return status(200).entity(osgiService.update(accountPid, config)).build();
        } catch (ConfigNotFoundException e) {
            return status(404).entity(generateErrorMessage(e.getMessage())).build();
        } catch (IOException e) {
            return status(500).build();
        }
    }

    // Delete an account
    @Path("/account/{account_pid}")
    @DELETE
    public Response deleteAccount(@PathParam("account_pid") String accountPid) {
        try {
            osgiService.delete(accountPid);
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

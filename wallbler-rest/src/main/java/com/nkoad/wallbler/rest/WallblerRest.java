package com.nkoad.wallbler.rest;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/wallbler")
@Component(name = "Wallbler Rest Service", service = WallblerRest.class, property = {"osgi.jaxrs.resource=true"})
public class WallblerRest {
    @Path("/")
    @Produces("application/json")
    @GET
    public String getData() {
        System.out.println("-------------");
        System.out.println("-------------");
        return "Hello Restful service";
    }

}

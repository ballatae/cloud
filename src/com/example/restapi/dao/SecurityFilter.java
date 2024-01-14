package com.example.restapi.dao;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;


/**
 * This filter verify the access permissions for a user
 * based on username and passowrd provided in request
 * */
@Provider
public class SecurityFilter implements jakarta.ws.rs.container.ContainerRequestFilter
{
	
	@Context
    private ResourceInfo resourceInfo;
	private RecordDAO dao = RecordDAO.getInstance();
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "JWT";
     
    @Override
    public void filter(ContainerRequestContext requestContext)
    {
        Method method = resourceInfo.getResourceMethod();
        //Access allowed for all
        if( ! method.isAnnotationPresent(PermitAll.class))
        {
            //Access denied for all
            if(method.isAnnotationPresent(DenyAll.class))
            {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                         .entity("Access blocked for all users !!").build());
                return;
            }

            //Verify user access
            if(method.isAnnotationPresent(RolesAllowed.class))
            {
            	//Get request headers
                final MultivaluedMap<String, String> headers = requestContext.getHeaders();
                 
                //Fetch authorization header
                final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);
                 
                //If no authorization information present; block access
                if(authorization == null || authorization.isEmpty())
                {
                	requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                	        .header("Unauthorized-Reason", "You are not authorized to acces this resource")
                	        .build());
                    return;
                }
                 try {
                //Get encoded username and password
                final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");
                
                DecodedJWT decodedToken = RecordResource.verifyToken(encodedUserPassword);
                String username = decodedToken.getClaim("username").asString();
                String password = decodedToken.getClaim("password").asString();
                
                
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));
                 
                //Is user valid?
                if( ! isUserAllowed(username, password, rolesSet))
                {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("You are not authorized to acces this resource").build());
                    return;
                }
                 }
                 catch( Exception e) {
                	 requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                             .entity("You are not authorized to acces this resource").build());
                         return;
                 }
            }
        }
    }
    
    private boolean isUserAllowed( String username, String password,  Set<String> rolesSet)
    {
        boolean isAllowed = false;
    
        
    	String userRole = dao.getUserRole(username);
    	
        
        //Step 2. Verify user role
        if(rolesSet.contains(userRole))
        {
            isAllowed = true;
        }
        
        return isAllowed;
    }
}

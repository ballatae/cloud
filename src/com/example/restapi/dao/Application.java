package com.example.restapi.dao;

import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {

	public Application(){
		packages("com.example.restapi.dao");

		//Register Auth Filter here
		register(SecurityFilter.class);
	}
	
}

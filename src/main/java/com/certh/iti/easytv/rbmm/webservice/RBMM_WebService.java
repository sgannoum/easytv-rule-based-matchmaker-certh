package com.certh.iti.easytv.rbmm.webservice;


import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.certh.iti.easytv.rbmm.reasoner.RuleReasoner;
import com.certh.iti.easytv.rbmm.user.OntProfile;
import com.certh.iti.easytv.user.exceptions.UserProfileParsingException;

//http://localhost:8080/EasyTV_RBMM_Restful_WS/rules
@Path("/")
public class RBMM_WebService
{
	private final static Logger logger = Logger.getLogger(RBMM_WebService.class.getName());

	private static final String ONTOLOGY_NAME = RBMM_config.ONTOLOGY_NAME;
	private static final String[] RULES_FILE = RBMM_config.RULES_FILE;

	private static RuleReasoner ruleReasoner = null;
	
	public RBMM_WebService() {
		
		if(ruleReasoner == null) {
			try {
				ruleReasoner = new RuleReasoner(ONTOLOGY_NAME, RULES_FILE);
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
	
    @GET
    @Path("personalize/rules")
    @Produces("application/json")
    public Response getRules() throws IOException, JSONException
    {
    	logger.info("get rules request...");
		
		//convert to json array
		JSONArray jsonRules = ruleReasoner.getRules();
				
        return Response.status(200).entity(jsonRules.toString(4)).build();
    }
    
    @POST
    @Path("personalize/rules")
    @Consumes("application/json")
    @Produces("application/json")
    public Response postRules(Object tmpInput) throws IOException, JSONException
    {
    	logger.info("post rules request...");
    	
    	JSONArray jsonRules = new JSONArray((Collection<?>)tmpInput);
    	
    	ruleReasoner.updateRules(jsonRules);
				
        return Response.status(200).entity("OK").build();
    }
	
    @POST
    @Path("personalize/profile")
    @Consumes("application/json")
    public Response personalizeProfile(Object tmpInput) throws IOException, JSONException
    {
    	logger.info("Personalize profile request...");
    	
    	JSONObject response;
    	JSONObject json = new JSONObject((Map<?, ?>)tmpInput);	
    	
    	//user_id
		if(!json.has("user_id")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_id element");
			logger.info(err.toString());
			return Response.status(400).entity(err).build();
		}
    	
		//user_profile
		if(!json.has("user_profile")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_profile element");
			logger.info(err.toString());
			return Response.status(400).entity(err).build();
		}
		

		//Unmarshal inputs
		int user_id = json.getInt("user_id");
		OntProfile ontProfile = null;
		
	
		//load user file
		try 
		{
			ontProfile = new OntProfile(json);
		} catch (UserProfileParsingException e)
		{
			response = new JSONObject().put("user_id", user_id).put("msg:", e.getMessage());
	    	logger.info("["+user_id+"][Error]: "+ response.toString());
			return Response.status(400).entity(response.toString(4)).build();
		}
		

		//personalize context
		JSONObject personalized_profile = ruleReasoner.infer(ontProfile);
		
		//log
    	logger.info("["+user_id+"]: "+ personalized_profile.toString());
		
        return Response.status(200).entity(personalized_profile.toString(4)).build();
    }
    
    @POST
    @Path("personalize/context")
    @Consumes("application/json")
    public Response personalizeContext(Object tmpInput) throws IOException, JSONException
    {
    	
    	logger.info("Personalize context request...");

    	
    	JSONObject response;
    	JSONObject json = new JSONObject((Map<?, ?>)tmpInput);
    	
    	//user_id
		if(!json.has("user_id")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_id element");
			logger.info(err.toString());
			return Response.status(400).entity(err).build();
		}
    	
		//user_profile
		if(!json.has("user_profile")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_profile element");
			logger.info(err.toString());
			return Response.status(400).entity(err).build();
		}
	
		//context
		if(!json.has("user_context")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_context element");
			logger.info( err.toString());
			return Response.status(400).entity(err).build();
		}
		
		
		int user_id = json.getInt("user_id");
		OntProfile ontProfile = null;
		
	
		//load user file
		try 
		{
			ontProfile = new OntProfile(json);
		} catch (UserProfileParsingException e)
		{
			response = new JSONObject().put("user_id", user_id).put("msg:", e.getMessage());
	    	logger.info("["+user_id+"][Error]: "+ response.toString());
			return Response.status(400).entity(response.toString(4)).build();
		}
		

		//personalize context
		JSONObject personalized_profile = ruleReasoner.infer(ontProfile);

		//log
    	logger.info("["+user_id+"]: "+ personalized_profile.toString());
    	
        return Response.status(200).entity(personalized_profile.toString(4)).build();
    }
    
    @POST
    @Path("personalize/content")
    @Consumes("application/json")
    public Response personalizeContent(Object tmpInput) throws IOException, JSONException
    {
    	
    	logger.info("Personalize content request...");

    	JSONObject response;
    	JSONObject json = new JSONObject((Map<?, ?>)tmpInput);	
    	
    	//user_id
		if(!json.has("user_id")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_id element");
			logger.info(err.toString());
			return Response.status(400).entity(err).build();
		}
    	
		//user_profile
		if(!json.has("user_profile")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_profile element");
			logger.info(err.toString());
			return Response.status(400).entity(err).build();
		}
		
		//content
		if(!json.has("user_content")) {
			JSONObject err = new JSONObject().put("code", 400).put("msg", "Missing user_content element");
			logger.info( err.toString());
			return Response.status(400).entity(err).build();
		}
		
		
		int user_id = json.getInt("user_id");
		OntProfile ontProfile = null;
		
	
		//load user file
		try 
		{
			ontProfile = new OntProfile(json);
		} catch (UserProfileParsingException e)
		{
			response = new JSONObject().put("user_id", user_id).put("msg:", e.getMessage());
	    	logger.info("["+user_id+"][Error]: "+ response.toString());
			return Response.status(400).entity(response.toString(4)).build();
		}
		

		//personalize context
		JSONObject personalized_profile = ruleReasoner.inferContentSuggestions(ontProfile);

		
		//log
    	logger.info("["+user_id+"]: "+ personalized_profile.toString());
		
        return Response.status(200).entity(personalized_profile.toString(4)).build();
    }
}

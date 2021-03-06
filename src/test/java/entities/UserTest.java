package entities;

import java.io.IOException;
import java.io.InputStream;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.certh.iti.easytv.rbmm.builtin.And;
import com.certh.iti.easytv.rbmm.builtin.Equals;
import com.certh.iti.easytv.rbmm.builtin.GreaterThan;
import com.certh.iti.easytv.rbmm.builtin.GreaterThanEquals;
import com.certh.iti.easytv.rbmm.builtin.LessThan;
import com.certh.iti.easytv.rbmm.builtin.LessThanEquals;
import com.certh.iti.easytv.rbmm.builtin.MergePreferences;
import com.certh.iti.easytv.rbmm.builtin.NOT;
import com.certh.iti.easytv.rbmm.builtin.NotEquals;
import com.certh.iti.easytv.rbmm.builtin.OR;
import com.certh.iti.easytv.rbmm.user.OntProfile;
import com.certh.iti.easytv.rbmm.user.OntUserProfile;
import com.certh.iti.easytv.rbmm.user.preference.OntPreference;
import com.certh.iti.easytv.user.exceptions.UserProfileParsingException;
import com.fasterxml.jackson.core.JsonParseException;
import comparatorOperand.EqualsRulesTest;
import comparatorOperand.GreaterThanEqualRulesTest;
import comparatorOperand.GreaterThanRulesTest;
import comparatorOperand.LessThanEqualRulesTest;
import comparatorOperand.LessThanRulesTest;
import comparatorOperand.NotEqualsRulesTest;
import config.RBMMTestConfig;
import junit.framework.Assert;
import logicalOperand.AndRulesTest;
import logicalOperand.NotRulesTest;
import logicalOperand.OrRulesTest;

public class UserTest {
	
	private OntModel model;
	public static final String 	suggestionRule =  
	"[font_size_suggestion:" + 
			" (?user http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#User) " + 
			" (?user http://www.owl-ontologies.com/OntologyEasyTV.owl#hasPreference ?pref)" +
			" (?user http://www.owl-ontologies.com/OntologyEasyTV.owl#hasSuggestionSet ?sugSet)" + 
			" (?pref "+OntPreference.getPredicate("http://registry.easytv.eu/application/cs/accessibility/magnification/scale")+" ?mg)" + 
			" GT(?mg, '1.0'^^http://www.w3.org/2001/XMLSchema#double, ?res)	" +
			" makeTemp(?sugPref)" + 
			"->" + 
			" (?sugSet http://www.owl-ontologies.com/OntologyEasyTV.owl#hasSuggestion ?sugPref)" +
			" (?sugPref http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#SuggestedPreferences)" +
			" (?sugPref http://www.owl-ontologies.com/OntologyEasyTV.owl#hasConfidence '0.5'^^http://www.w3.org/2001/XMLSchema#double)" +
			" (?sugPref "+OntPreference.getPredicate("http://registry.easytv.eu/application/cs/ui/text/size")+" '60'^^http://www.w3.org/2001/XMLSchema#int)" + 
	 "]";
	
	private String rules =  AndRulesTest.rules + OrRulesTest.rules + NotRulesTest.rules +
			EqualsRulesTest.rules + NotEqualsRulesTest.rules +
			GreaterThanRulesTest.rules + GreaterThanEqualRulesTest.rules +
			LessThanRulesTest.rules + LessThanEqualRulesTest.rules +
			ConditionsTest.rules + suggestionRule
			;
	
	@BeforeMethod
	public void beforeMethod() throws IOException {
		
		//Read model
		model = ModelFactory.createOntologyModel();
		InputStream in = ClassLoader.getSystemResourceAsStream(RBMMTestConfig.ONTOLOGY_File);
		model = (OntModel) model.read(in, null, "");
		in.close();
		
		//Add built in functions
		BuiltinRegistry.theRegistry.register("NE", new NotEquals());
		BuiltinRegistry.theRegistry.register("EQ", new Equals());
		BuiltinRegistry.theRegistry.register("GT", new GreaterThan());
		BuiltinRegistry.theRegistry.register("GE", new GreaterThanEquals());
		BuiltinRegistry.theRegistry.register("LT", new LessThan());
		BuiltinRegistry.theRegistry.register("LE", new LessThanEquals());
		BuiltinRegistry.theRegistry.register("and", new And());
		BuiltinRegistry.theRegistry.register("or", new OR());
		BuiltinRegistry.theRegistry.register("not", new NOT());
		BuiltinRegistry.theRegistry.register(new MergePreferences());

		System.out.println("Ontology was loaded");

	}
	
	@Test
	public void TestUserInference() 
	  throws JsonParseException, IOException, UserProfileParsingException {
		
		JSONObject userProfile = RBMMTestConfig.getProfile("profile_with_context_1.json");
	 
		OntProfile ontPRofile = new OntProfile(userProfile);
		Individual userInstance = ontPRofile.createOntologyInstance(model);
				
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Property hasSuggestionSetProperty = model.getProperty(OntUserProfile.HAS_SUGGESTION_SET_PROPERTY);
		Statement suggestionSet = inf.listStatements(userInstance, hasSuggestionSetProperty, (RDFNode)null).next();
				
		Property hasSuggestedPreferenceProperty = model.getProperty(OntUserProfile.HAS_SUGGESTION_PROPERTY);
		StmtIterator userList = inf.listStatements(suggestionSet.getObject().asResource(), hasSuggestedPreferenceProperty, (RDFNode)null);
		Resource userSuggestedPreferenceInstance = userList.next().getObject().asResource();
		StmtIterator userPreferenceList = inf.listStatements(userSuggestedPreferenceInstance, null, (RDFNode)null);
		Property hasFontSizeProperty = model.getProperty(OntPreference.getPredicate("http://registry.easytv.eu/application/cs/ui/text/size"));
		 userPreferenceList = inf.listStatements(userSuggestedPreferenceInstance, hasFontSizeProperty, (RDFNode)null);
		Assert.assertEquals(60, userPreferenceList.next().getObject().asLiteral().getInt());
		Assert.assertFalse(userPreferenceList.hasNext());

	}
	
	@Test
	public void TestUserInference1() 
	  throws JsonParseException, IOException, UserProfileParsingException {
	 
		JSONObject userProfile = RBMMTestConfig.getProfile("profile_with_context_2.json");
		 
		OntProfile ontPRofile = new OntProfile(userProfile);
		Individual userInstance = ontPRofile.createOntologyInstance(model);
		
				
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Property hasPreferenceProperty = model.getProperty(OntUserProfile.HAS_PREFERENCE_PROP);
		StmtIterator userList = inf.listStatements(userInstance, hasPreferenceProperty, (RDFNode)null);
		Resource userPreferenceInstance = userList.next().getObject().asResource();
		
		StmtIterator userPreferenceList = inf.listStatements(userPreferenceInstance, null, (RDFNode)null);
		Property hasAudioVolumeProperty = model.getProperty(OntPreference.getPredicate("http://registry.easytv.eu/application/cs/audio/volume"));
		userPreferenceList = inf.listStatements(userPreferenceInstance, hasAudioVolumeProperty, (RDFNode)null);		
		Assert.assertEquals(10, userPreferenceList.next().getObject().asLiteral().getInt());
		Assert.assertFalse(userPreferenceList.hasNext());
		
		Property hasFontSizeProperty = model.getProperty(OntPreference.getPredicate("http://registry.easytv.eu/application/cs/ui/text/size"));
		userPreferenceList = inf.listStatements(userPreferenceInstance, hasFontSizeProperty, (RDFNode)null);
		Assert.assertEquals(20, userPreferenceList.next().getObject().asLiteral().getInt());
		Assert.assertFalse(userPreferenceList.hasNext());

	}

	
	@Test
	public void TestUserInference2() 
	  throws JsonParseException, IOException, UserProfileParsingException {
	 
		JSONObject userProfile = RBMMTestConfig.getProfile("profile_with_context_3.json");
		
	 
		OntProfile ontPRofile = new OntProfile(userProfile);
		Individual userInstance = ontPRofile.createOntologyInstance(model);
				
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Property hasPreferenceProperty = model.getProperty(OntUserProfile.HAS_PREFERENCE_PROP);
		StmtIterator userList = inf.listStatements(userInstance, hasPreferenceProperty, (RDFNode)null);
		Resource userPreferenceInstance = userList.next().getObject().asResource();
		
		StmtIterator userPreferenceList = inf.listStatements(userPreferenceInstance, null, (RDFNode)null);
		Property hasAudioVolumeProperty = model.getProperty(OntPreference.getPredicate("http://registry.easytv.eu/common/volume"));
		userPreferenceList = inf.listStatements(userPreferenceInstance, hasAudioVolumeProperty, (RDFNode)null);
		Assert.assertEquals(10, userPreferenceList.next().getObject().asLiteral().getInt());
		Assert.assertFalse(userPreferenceList.hasNext());

	}
	
	@Test
	public void TestUserInference3() 
	  throws JsonParseException, IOException, UserProfileParsingException {
			
		JSONObject userProfile = RBMMTestConfig.getProfile("profile_with_context_4.json");
		 
		OntProfile ontPRofile = new OntProfile(userProfile);
		Individual userInstance = ontPRofile.createOntologyInstance(model);
		
				
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Property hasPreferenceProperty = model.getProperty(OntUserProfile.HAS_PREFERENCE_PROP);
		StmtIterator userList = inf.listStatements(userInstance, hasPreferenceProperty, (RDFNode)null);
		Resource userPreferenceInstance = userList.next().getObject().asResource();
		
		StmtIterator userPreferenceList = inf.listStatements(userPreferenceInstance, null, (RDFNode)null);
		Property hasAudioVolumeProperty = model.getProperty(OntPreference.getPredicate("http://registry.easytv.eu/common/volume"));
		userPreferenceList = inf.listStatements(userPreferenceInstance, hasAudioVolumeProperty, (RDFNode)null);
		Assert.assertEquals(36, userPreferenceList.next().getObject().asLiteral().getInt());
		Assert.assertFalse(userPreferenceList.hasNext());
		
		Property hasBackgroundProperty = model.getProperty(OntPreference.getPredicate("http://registry.easytv.eu/common/display/screen/enhancement/cursor/color"));
		userPreferenceList = inf.listStatements(userPreferenceInstance, hasBackgroundProperty, (RDFNode)null);
		Assert.assertEquals("#222222", userPreferenceList.next().getObject().asLiteral().getString());
		Assert.assertFalse(userPreferenceList.hasNext());
	}

}

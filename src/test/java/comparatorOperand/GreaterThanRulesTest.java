package comparatorOperand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.certh.iti.easytv.rbmm.builtin.GreaterThan;
import com.certh.iti.easytv.rbmm.user.User;
import com.certh.iti.easytv.rbmm.user.UserContext;
import com.certh.iti.easytv.rbmm.user.UserPreference;
import com.certh.iti.easytv.rbmm.user.UserPreferencesMappings;
import com.certh.iti.easytv.rbmm.user.preference.Conditions;
import com.certh.iti.easytv.rbmm.user.preference.Preferences;

import config.RBMMTestConfig;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

public class GreaterThanRulesTest {
	
	private OntModel model;
	public static final String 	rules = "[Greater_than:" + 
		    "(?cond http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#GT)" +
		    ",(?cond http://www.owl-ontologies.com/OntologyEasyTV.owl#hasValue ?value)" + 
		    ",(?cond http://www.owl-ontologies.com/OntologyEasyTV.owl#hasType ?type)" + 
		    ",(?user http://www.w3.org/1999/02/22-rdf-syntax-ns#type "+User.ONTOLOGY_CLASS_URI+")" + 
		    ",(?user "+User.HAS_PREFERENCE_PROP+" ?pref)" + 
		    ",(?pref ?type ?nodeValue)" + 
		    "->" + 
			"	greaterThan(?nodeValue, ?value, ?res)"+
		    "	(?cond http://www.owl-ontologies.com/OntologyEasyTV.owl#isTrue ?res)" +
		    "	print('Greater Than', ?nodeValue, ?value, ?res)"+
		    "]" +
		    "[Greater_than_context:" + 
		    "(?cond http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#GT)" +
		    ",(?cond http://www.owl-ontologies.com/OntologyEasyTV.owl#hasValue ?value)" + 
		    ",(?cond http://www.owl-ontologies.com/OntologyEasyTV.owl#hasType ?type)" + 
		    ",(?user http://www.w3.org/1999/02/22-rdf-syntax-ns#type "+User.ONTOLOGY_CLASS_URI+")" + 
		    ",(?user "+User.HAS_CONTEXT_PROP+" ?context)" + 
		    ",(?context ?type ?nodeValue)" + 
		    "->" + 
			"	greaterThan(?nodeValue, ?value, ?res)"+
		    "	(?cond http://www.owl-ontologies.com/OntologyEasyTV.owl#isTrue ?res)" +
		    "	print('Greater Than', ?nodeValue, ?value, ?res)"+
		    "]"
		    
		    ;
	
	@BeforeMethod
	public void beforeMethod() throws FileNotFoundException {
		
		File file = new File(RBMMTestConfig.ONTOLOGY_File);
		model = ModelFactory.createOntologyModel();
		InputStream in = new FileInputStream(file);
		model = (OntModel) model.read(in, null, "");
		BuiltinRegistry.theRegistry.register(new GreaterThan());
		System.out.println("Ontology was loaded");
		
		//user context
		OntClass userContextClass = model.getOntClass(UserContext.ONTOLOGY_CLASS_URI);
		Individual  userContextInstance = userContextClass.createIndividual();
		
		Property hasTimeProperty = model.getProperty(UserContext.HAS_TIME_PROP);
		userContextInstance.addProperty(hasTimeProperty, model.createTypedLiteral("2019-05-30T09:47:47.619Z"));
		
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreference.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preferences.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		OntClass userClass = model.getOntClass(User.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(User.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
		Property hasContextProperty = model.getProperty(User.HAS_CONTEXT_PROP);
		userInstance.addProperty(hasContextProperty, userContextInstance);
		
	}

	@Test
	public void Test_greaterThanIsTrue()  {
		
		//gt
		OntClass gtClass = model.getOntClass(Conditions.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Conditions.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Conditions.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(5));
		 
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
				
		Property isTrueProperty = model.getProperty(Conditions.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(null, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		while (list.hasNext()) {
			Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), true);
		}
		
	}
	
	
	@Test
	public void Test_greaterThanIsFalse()  {
		
		//gt
		OntClass gtClass = model.getOntClass(Conditions.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Conditions.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Conditions.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
				
		Property isTrueProperty = model.getProperty(Conditions.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(null, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		while (list.hasNext()) {
			Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), false);
		}
		
	}
	
	@Test
	public void Test_greaterThan_Date_true()  {
		
		//gt
		OntClass gtClass = model.getOntClass(Conditions.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Conditions.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/context/time")));
				
		Property hasValueProperty = model.getProperty(Conditions.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral("2019-04-30T09:47:47.619Z" ));
		 
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
				
		Property isTrueProperty = model.getProperty(Conditions.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(null, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		while (list.hasNext()) {
			Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), true);
		}
		
	}
	
	
	@Test
	public void Test_greaterThan_Date_false()  {
		
		//gt
		OntClass gtClass = model.getOntClass(Conditions.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Conditions.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/context/time")));
				
		Property hasValueProperty = model.getProperty(Conditions.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral("2019-05-30T09:47:47.619Z" ));
		
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
				
		Property isTrueProperty = model.getProperty(Conditions.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(null, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		while (list.hasNext()) {
			Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), false);
		}
		
	}
}

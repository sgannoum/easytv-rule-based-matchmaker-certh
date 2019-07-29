package rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
import org.testng.Assert;
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
import com.certh.iti.easytv.rbmm.user.UserProfile;
import com.certh.iti.easytv.rbmm.user.UserPreferences;
import com.certh.iti.easytv.rbmm.user.UserPreferencesMappings;
import com.certh.iti.easytv.rbmm.user.preference.Condition;
import com.certh.iti.easytv.rbmm.user.preference.Preference;
import com.fasterxml.jackson.core.JsonParseException;

import comparatorOperand.EqualsRulesTest;
import comparatorOperand.GreaterThanEqualRulesTest;
import comparatorOperand.GreaterThanRulesTest;
import comparatorOperand.LessThanEqualRulesTest;
import comparatorOperand.LessThanRulesTest;
import comparatorOperand.NotEqualsRulesTest;
import config.RBMMTestConfig;
import entities.ConditionsTest;
import logicalOperand.AndRulesTest;
import logicalOperand.NotRulesTest;
import logicalOperand.OrRulesTest;

public class AllRulesTests {
	
	private OntModel model;
	private String rules =  AndRulesTest.rules + OrRulesTest.rules + NotRulesTest.rules +
							EqualsRulesTest.rules + NotEqualsRulesTest.rules +
							GreaterThanRulesTest.rules + GreaterThanEqualRulesTest.rules +
							LessThanRulesTest.rules + LessThanEqualRulesTest.rules +
							ConditionsTest.rules
							;
	
	@BeforeMethod
	public void beforeMethod() throws FileNotFoundException {
		
		File file = new File(RBMMTestConfig.ONTOLOGY_File);
		model = ModelFactory.createOntologyModel();
		InputStream in = new FileInputStream(file);
		model = (OntModel) model.read(in, null, "");
		BuiltinRegistry.theRegistry.register(new NotEquals());
		BuiltinRegistry.theRegistry.register(new Equals());
		BuiltinRegistry.theRegistry.register(new GreaterThan());
		BuiltinRegistry.theRegistry.register(new GreaterThanEquals());
		BuiltinRegistry.theRegistry.register(new LessThan());
		BuiltinRegistry.theRegistry.register(new LessThanEquals());
		BuiltinRegistry.theRegistry.register(new And());
		BuiltinRegistry.theRegistry.register(new OR());
		BuiltinRegistry.theRegistry.register(new NOT());
		BuiltinRegistry.theRegistry.register(new MergePreferences());

		System.out.println("Ontology was loaded");

	}

	@Test
	public void Test_And_True_True_input()  {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(3));
		
		//lt
		OntClass ltClass = model.getOntClass(Condition.NAMESPACE + "LT");
		Individual ltInstance = ltClass.createIndividual();
		
		ltInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
		ltInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		//and
		OntClass andClass = model.getOntClass(Condition.NAMESPACE + "AND");
		Individual andInstance = andClass.createIndividual();

		Property hasLeftOperandProperty = model.getProperty(Condition.LEFT_OPERAND_PROP);
		andInstance.addProperty(hasLeftOperandProperty, gtInstance);
		
		Property hasRightOperandProperty = model.getProperty(Condition.RIGHT_OPERAND_PROP);
		andInstance.addProperty(hasRightOperandProperty, ltInstance);
				
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
			
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(andInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), true);
		Assert.assertFalse(list.hasNext());	
	}
	
	
	@Test
	public void Test_And_False_True_input()  {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		//lt
		OntClass ltClass = model.getOntClass(Condition.NAMESPACE + "LT");
		Individual ltInstance = ltClass.createIndividual();
		
		ltInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
		ltInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		//and
		OntClass andClass = model.getOntClass(Condition.NAMESPACE + "AND");
		Individual andInstance = andClass.createIndividual();

		Property hasLeftOperandProperty = model.getProperty(Condition.LEFT_OPERAND_PROP);
		andInstance.addProperty(hasLeftOperandProperty, gtInstance);
		
		Property hasRightOperandProperty = model.getProperty(Condition.RIGHT_OPERAND_PROP);
		andInstance.addProperty(hasRightOperandProperty, ltInstance);
				
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
			
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(andInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), false);
		Assert.assertFalse(list.hasNext());	
	}
	
	@Test
	public void Test_And_True_False_input()  {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(3));
		
		//lt
		OntClass ltClass = model.getOntClass(Condition.NAMESPACE + "LT");
		Individual ltInstance = ltClass.createIndividual();
		
		ltInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
		ltInstance.addProperty(hasValueProperty, model.createTypedLiteral(5));
		
		//and
		OntClass andClass = model.getOntClass(Condition.NAMESPACE + "AND");
		Individual andInstance = andClass.createIndividual();

		Property hasLeftOperandProperty = model.getProperty(Condition.LEFT_OPERAND_PROP);
		andInstance.addProperty(hasLeftOperandProperty, gtInstance);
		
		Property hasRightOperandProperty = model.getProperty(Condition.RIGHT_OPERAND_PROP);
		andInstance.addProperty(hasRightOperandProperty, ltInstance);
				
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
			
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(andInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), false);
		Assert.assertFalse(list.hasNext());
	}
	
	@Test
	public void Test_And_False_False_input()  {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		//lt
		OntClass ltClass = model.getOntClass(Condition.NAMESPACE + "LT");
		Individual ltInstance = ltClass.createIndividual();
		
		ltInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
		ltInstance.addProperty(hasValueProperty, model.createTypedLiteral(5));
		
		//and
		OntClass andClass = model.getOntClass(Condition.NAMESPACE + "AND");
		Individual andInstance = andClass.createIndividual();

		Property hasLeftOperandProperty = model.getProperty(Condition.LEFT_OPERAND_PROP);
		andInstance.addProperty(hasLeftOperandProperty, gtInstance);
		
		Property hasRightOperandProperty = model.getProperty(Condition.RIGHT_OPERAND_PROP);
		andInstance.addProperty(hasRightOperandProperty, ltInstance);
				
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
			
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(andInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), false);
		Assert.assertFalse(list.hasNext());

	}
	
	@Test
	public void Test_Not_False_input()  {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		//lt
		OntClass ltClass = model.getOntClass(Condition.NAMESPACE + "LT");
		Individual ltInstance = ltClass.createIndividual();
		
		ltInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
		ltInstance.addProperty(hasValueProperty, model.createTypedLiteral(5));
		
		//and
		OntClass andClass = model.getOntClass(Condition.NAMESPACE + "AND");
		Individual andInstance = andClass.createIndividual();

		Property hasLeftOperandProperty = model.getProperty(Condition.LEFT_OPERAND_PROP);
		andInstance.addProperty(hasLeftOperandProperty, gtInstance);
		
		Property hasRightOperandProperty = model.getProperty(Condition.RIGHT_OPERAND_PROP);
		andInstance.addProperty(hasRightOperandProperty, ltInstance);
		
		
		//not
		OntClass notClass = model.getOntClass(Condition.NAMESPACE + "NOT");
		Individual notInstance = notClass.createIndividual();

		notInstance.addProperty(hasLeftOperandProperty, andInstance);
				

		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
			
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(notInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), true);
		Assert.assertFalse(list.hasNext());
	}
	
	@Test
	public void Test_conditional_Preferences_true() 
	  throws JsonParseException, IOException {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property hasFontSizeProperty = model.getProperty(Preference.FONT_SIZE_PROP);
		userPreferenceInstance.addProperty(hasFontSizeProperty, model.createTypedLiteral(3));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
	
		//Add conditional preferences
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(1));
		
		//lt
		OntClass ltClass = model.getOntClass(Condition.NAMESPACE + "LT");
		Individual ltInstance = ltClass.createIndividual();
		
		ltInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/display/screen/enhancement/font/size")));
		ltInstance.addProperty(hasValueProperty, model.createTypedLiteral(7));
		
		//and
		OntClass andClass = model.getOntClass(Condition.NAMESPACE + "AND");
		Individual andInstance = andClass.createIndividual();

		Property hasLeftOperandProperty = model.getProperty(Condition.LEFT_OPERAND_PROP);
		andInstance.addProperty(hasLeftOperandProperty, gtInstance);
		
		Property hasRightOperandProperty = model.getProperty(Condition.RIGHT_OPERAND_PROP);
		andInstance.addProperty(hasRightOperandProperty, ltInstance);
		
		
		//conditional
		OntClass conditionalPreferenceClass = model.getOntClass(Condition.ONTOLOGY_CLASS_URI);
		Individual conditionalPreferenceInstance = conditionalPreferenceClass.createIndividual();
		
		Property hasNameProperty = model.getProperty(Preference.HAS_NAME_PROP);
		conditionalPreferenceInstance.addProperty(hasNameProperty,model.createTypedLiteral("condition_1"));
		
		Property hasConditionsProperty = model.getProperty(Condition.HAS_CONDITIONS_PROP);
		conditionalPreferenceInstance.addProperty(hasConditionsProperty, andInstance) ;
	
		conditionalPreferenceInstance.addProperty(hasFontSizeProperty, model.createTypedLiteral(500));

		conditionalPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(600));
		
		
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(andInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), true);
		Assert.assertFalse(list.hasNext());

			
		list = inf.listStatements(userPreferenceInstance, hasAudioVolumeProperty, (RDFNode)null);
		Assert.assertEquals(list.next().getObject().asLiteral().getInt(), 600);
		Assert.assertFalse(list.hasNext());

		//check font size
		list = inf.listStatements(userPreferenceInstance, hasFontSizeProperty, (RDFNode)null);
		Assert.assertEquals(list.next().getObject().asLiteral().getInt(), 500);
		Assert.assertFalse(list.hasNext());
		
		//check font size
		list = inf.listStatements(userPreferenceInstance, cursorSizeProperty, (RDFNode)null);
		Assert.assertEquals(list.next().getObject().asLiteral().getInt(), 10);
		Assert.assertFalse(list.hasNext());
	}
	
	
	@Test
	public void Test_simple_conditional_Preferences_true() 
	  throws JsonParseException, IOException {
		
		//user
		OntClass userPreferenceClass = model.getOntClass(UserPreferences.ONTOLOGY_CLASS_URI);
		Individual  userPreferenceInstance = userPreferenceClass.createIndividual();
		
		Property hasAudioVolumeProperty = model.getProperty(Preference.AUDIO_VOLUME_PROP);
		userPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(6));
		
		Property hasFontSizeProperty = model.getProperty(Preference.FONT_SIZE_PROP);
		userPreferenceInstance.addProperty(hasFontSizeProperty, model.createTypedLiteral(3));
		
		Property cursorSizeProperty = model.getProperty(Preference.CURSOR_SIZE_PROP);
		userPreferenceInstance.addProperty(cursorSizeProperty, model.createTypedLiteral(10));
		
		OntClass userClass = model.getOntClass(UserProfile.ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		Property hasPreferenceProperty = model.getProperty(UserProfile.HAS_PREFERENCE_PROP);
		userInstance.addProperty(hasPreferenceProperty, userPreferenceInstance);
		
	
		//Add conditional preferences
		//gt
		OntClass gtClass = model.getOntClass(Condition.NAMESPACE + "GT");
		Individual gtInstance = gtClass.createIndividual();

		Property hasTypeProperty = model.getProperty(Condition.HAS_TYPE_PROP);
		gtInstance.addProperty(hasTypeProperty, model.createProperty(UserPreferencesMappings.getDataProperty("http://registry.easytv.eu/common/content/audio/volume")));
				
		Property hasValueProperty = model.getProperty(Condition.HAS_VALUE_PROP);
		gtInstance.addProperty(hasValueProperty, model.createTypedLiteral(1));
		
		
		//conditional
		OntClass conditionalPreferenceClass = model.getOntClass(Condition.ONTOLOGY_CLASS_URI);
		Individual conditionalPreferenceInstance = conditionalPreferenceClass.createIndividual();
		
		Property hasNameProperty = model.getProperty(Preference.HAS_NAME_PROP);
		conditionalPreferenceInstance.addProperty(hasNameProperty,model.createTypedLiteral("condition_1"));
		
		Property hasConditionsProperty = model.getProperty(Condition.HAS_CONDITIONS_PROP);
		conditionalPreferenceInstance.addProperty(hasConditionsProperty, gtInstance) ;
	
		conditionalPreferenceInstance.addProperty(hasFontSizeProperty, model.createTypedLiteral(500));

		conditionalPreferenceInstance.addProperty(hasAudioVolumeProperty, model.createTypedLiteral(600));
		
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(reasoner, model);
		
		Property isTrueProperty = model.getProperty(Condition.IS_TURE_PROP);
		StmtIterator list = inf.listStatements(gtInstance, isTrueProperty, (RDFNode)null);
		Assert.assertTrue(list.hasNext(), "No such statement "+isTrueProperty.getLocalName());
		Assert.assertEquals(list.next().getObject().asLiteral().getBoolean(), true);
		Assert.assertFalse(list.hasNext());

			
		list = inf.listStatements(userPreferenceInstance, hasAudioVolumeProperty, (RDFNode)null);
		Assert.assertEquals(list.next().getObject().asLiteral().getInt(), 600);
		Assert.assertFalse(list.hasNext());

		//check font size
		list = inf.listStatements(userPreferenceInstance, hasFontSizeProperty, (RDFNode)null);
		Assert.assertEquals(list.next().getObject().asLiteral().getInt(), 500);
		Assert.assertFalse(list.hasNext());
		
		//check font size
		list = inf.listStatements(userPreferenceInstance, cursorSizeProperty, (RDFNode)null);
		Assert.assertEquals(list.next().getObject().asLiteral().getInt(), 10);
		Assert.assertFalse(list.hasNext());
	}
	
}

package rule_matchmaker.entities;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;

public class User {
	
	private Visual visual;
	private Auditory auditory;
	private UserPreference user_preferences;
	
	private static final String NAMESPACE = "http://www.owl-ontologies.com/OntologyEasyTV.owl#";
	public static final String ONTOLOGY_CLASS_URI = NAMESPACE + "User";
	public static final String VISUAL_PROP = NAMESPACE + "hasVisualAbility";
	public static final String AUDITORY_PROP = NAMESPACE + "hasAuditoryAbility";
	public static final String PREFERENCE_PROP = NAMESPACE + "hasPreference";

	public Visual getVisual() {
		return visual;
	}


	public void setVisual(Visual visual) {
		this.visual = visual;
	}


	public Auditory getAuditory() {
		return auditory;
	}


	public void setAuditory(Auditory auditory) {
		this.auditory = auditory;
	}
	
	
	public UserPreference getUser_preferences() {
		return user_preferences;
	}


	public void setUser_preferences(UserPreference user_preferences) {
		this.user_preferences = user_preferences;
	}
	
	@Override
	public String toString() {
		return "User [visual=" + visual
				+ ", auditory=" + auditory + ", "
						+ "UserPreference= "+user_preferences+"]";
	}
	

	
	public Individual createOntologyInstance(final OntModel model){
		
		//create the new user in the ontology
		OntClass userClass = model.getOntClass(ONTOLOGY_CLASS_URI);
		Individual userInstance = userClass.createIndividual();
		
		//Add visual acuity
		Property hasVisualAbility = model.getProperty(VISUAL_PROP);
		userInstance.addProperty(hasVisualAbility, visual.createOntologyInstance(model));	
		
		//Add Auditory ability
		Property hasAuditoryAbility = model.getProperty(AUDITORY_PROP);
		userInstance.addProperty(hasAuditoryAbility, auditory.createOntologyInstance(model));	
		
		//Add user preferences
		Property hasPreferences = model.getProperty(PREFERENCE_PROP);
		userInstance.addProperty(hasPreferences, user_preferences.createOntologyInstance(model));	
		
		return userInstance;
	}

}

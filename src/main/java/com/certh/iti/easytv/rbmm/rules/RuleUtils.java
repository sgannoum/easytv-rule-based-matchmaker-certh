package com.certh.iti.easytv.rbmm.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.reasoner.rulesys.ClauseEntry;
import org.apache.jena.reasoner.rulesys.Functor;
import org.apache.jena.reasoner.rulesys.Node_RuleVariable;
import org.apache.jena.reasoner.rulesys.Rule;
import org.json.JSONArray;
import org.json.JSONObject;

import com.certh.iti.easytv.rbmm.user.OntUserContext;
import com.certh.iti.easytv.rbmm.user.Ontological;
import com.certh.iti.easytv.rbmm.user.preference.OntPreference;

/**
 * A set of utils to convert a jena rule from and to JSON format
 *
 */
public class RuleUtils {
	
	private final static Logger logger = java.util.logging.Logger.getLogger(RuleUtils.class.getName());


	public static JSONArray convert(List<Rule> rules) {
		
		JSONArray jsonRules = new JSONArray();
		for(Rule rule: rules)
			jsonRules.put(convert(rule));

		return jsonRules;
	}
	
	
	public static JSONObject convert(Rule rule) {
		
		logger.info("Conver rule "+rule.getName());
		
		HashMap<String, String> variableMpper = new HashMap<String, String>();
		JSONObject jsonRule = new JSONObject();
		if(rule.getName() != null) jsonRule.put("name", rule.getName());
		
		JSONArray bodyOperands = convert(rule.getBody(), variableMpper);
		jsonRule.put("body", bodyOperands);
		
		JSONArray headOperands = convert(rule.getHead(), variableMpper);
		jsonRule.put("head", headOperands);
		
		//find the rule confidence
		jsonRule.put("confidence", 0.0);
		for(ClauseEntry entry : rule.getHead()) 
			if(TriplePattern.class.isInstance(entry)) {
				//TripelPattern handling case
				
				TriplePattern triple = (TriplePattern) entry;
				if(triple.getPredicate().getURI().endsWith("hasConfidence")) {
					jsonRule.put("confidence", triple.getObject().getLiteralValue());
					break;
				}
			}

		return jsonRule;
	}
	
	/**
	 * Convert JSON given rules to jena rules
	 * @param jsonRules
	 * @return
	 */
	public static List<Rule> convert(JSONArray jsonRules) {
		
		List<Rule> rules = new ArrayList<Rule>();
		
		for(int i = 0; i < jsonRules.length(); i++) {
			JSONObject operand = (JSONObject) jsonRules.get(i);
			Rule rule = convert(operand);
			rules.add(rule);
		}

		return rules;
	}
	
	/**
	 * Convert from JSON to Jena rule
	 * @param rule
	 * @return
	 */
	public static Rule convert(JSONObject rule) {
			
		Map<String, String> vars = new HashMap<String, String>();
		StringBuffer buff = new StringBuffer();
		int var = 0;
	
		buff.append("[");
		if(rule.has("name")) buff.append(rule.getString("name")+":");
		buff.append("(?user http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#User)");
		buff.append("(?user http://www.owl-ontologies.com/OntologyEasyTV.owl#hasPreference ?pref)");
		buff.append("(?user http://www.owl-ontologies.com/OntologyEasyTV.owl#hasContext ?cnxt)");
		buff.append("(?user http://www.owl-ontologies.com/OntologyEasyTV.owl#hasSuggestionSet ?sugSet)");
		
		//Handle head
		JSONArray body = rule.getJSONArray("body");
		double confidence = rule.getDouble("confidence");
		for(int i = 0; i < body.length(); i++) {
			JSONObject statement = body.getJSONObject(i);
			
			String uri = statement.getString("preference");
			String functor = statement.getString("builtin");
			JSONArray args = statement.getJSONArray("args");
			
			String predicate = null;
			if((predicate = RuleUtils.getPredicate(uri)) == null) 
				throw new IllegalArgumentException("Unknown uri in rule head "+uri);
			
			String statment_var = uri.contains("context") ? "?cnxt":"?pref";
			
			for(int j = 0; j < args.length(); j++) {
				JSONObject arg = args.getJSONObject(j);			
	
				if(functor.equalsIgnoreCase("eq"))
					buff.append(String.format("(%s %s '%s'^^%s)\n", statment_var, predicate, arg.get("value"), arg.getString("xml-type")));
				else {
					//look for variable
					String prefVar;
					
					if((prefVar = vars.get(predicate)) == null) {
						prefVar = String.format("?var%d", var++);
						buff.append(String.format("(%s %s %s)\n", statment_var, predicate, prefVar));						
						vars.put(predicate, prefVar);
					} 
					
					buff.append(String.format("%S(%s '%s'^^%s)\n", functor, prefVar, arg.get("value"),  arg.getString("xml-type")));
				}
			}
		}
		
		
		//handle body
		JSONArray head = rule.getJSONArray("head");
		for(int i = 0; i < head.length(); i++) {
			JSONObject statement = head.getJSONObject(i);
			
			String preference = statement.getString("preference");
			String predicate = null;
			if((predicate = RuleUtils.getPredicate(preference)) == null) 
				throw new IllegalArgumentException("Unknown uri in rule body "+preference);
			
			JSONArray args = statement.getJSONArray("args");
			for(int j = 0; j < args.length(); j++) {
				JSONObject arg = args.getJSONObject(j);				
				buff.append(String.format("noValue(?pref %s '%s'^^%s)\n", predicate, arg.get("value"), arg.getString("xml-type")));
			}
		}
		
		
		//create a new suggestion and connect it with suggestions set
		buff.append("makeTemp(?ruleSug)");
		buff.append("makeTemp(?sugPref)");
		buff.append("->");
		buff.append("(?sugSet http://www.owl-ontologies.com/OntologyEasyTV.owl#hasSuggestion ?ruleSug)");
		buff.append("(?ruleSug http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#RuleSuggestion)");
		buff.append("(?ruleSug http://www.owl-ontologies.com/OntologyEasyTV.owl#hasConfidence "+String.format("'%.1f'^^http://www.w3.org/2001/XMLSchema#double)", confidence));
		buff.append("(?ruleSug http://www.owl-ontologies.com/OntologyEasyTV.owl#hasSuggestedPreferences ?sugPref)");
		buff.append("(?sugPref http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.owl-ontologies.com/OntologyEasyTV.owl#SuggestedPreferences)");

		//handle body
		for(int i = 0; i < head.length(); i++) {
			JSONObject statement = head.getJSONObject(i);
			
			String preference = statement.getString("preference");
			String predicate = null;
			if((predicate = RuleUtils.getPredicate(preference)) == null) 
				throw new IllegalArgumentException("Unknown uri in rule body "+preference);
			
			JSONArray args = statement.getJSONArray("args");
			for(int j = 0; j < args.length(); j++) {
				JSONObject arg = args.getJSONObject(j);				
				buff.append(String.format("(?sugPref %s '%s'^^%s)\n", predicate, arg.get("value"), arg.getString("xml-type")));
			}
		}

		buff.append("]\n");

		return Rule.parseRule(buff.toString());
	}
	
	private static JSONArray convert(ClauseEntry[] entries, HashMap<String, String> variableMpper) {
		JSONArray bodyOperands = new JSONArray();

		
		for(ClauseEntry entry : entries) {
			String uri = null;
			
			
			if(TriplePattern.class.isInstance(entry)) {
				//TripelPattern handling case
				
				TriplePattern t = (TriplePattern) entry;
				
				Node_RuleVariable subject = (Node_RuleVariable) t.getSubject();
				Node_URI predicate = (Node_URI) t.getPredicate();
				Node object = t.getObject();
				String predicateURI = predicate.getURI();

				//check that the predicate is a preference
				if(!predicateURI.startsWith(Ontological.NAMESPACE) && 
					 !predicateURI.contains("has_")) continue;
					
				//Convert to preference or contextual URI, just ignore unknown preferences
				if((uri = RuleUtils.getURI(predicateURI)) == null) {
					logger.info("Unknown predicate "+predicateURI);
					continue;
				}
				
				if(object.isLiteral()) {
					Node_Literal literal = (Node_Literal) object;
					JSONObject operand = new JSONObject()
											.put("builtin", "EQ")
											.put("preference", uri)
											.put("args", new JSONArray().put(new JSONObject()
																			.put("value", literal.getLiteralValue())
																			.put("xml-type", literal.getLiteralDatatypeURI())));
					
					bodyOperands.put(operand);
					
				} else if(object.isVariable()) {
					Node_RuleVariable variable = (Node_RuleVariable) object;
					variableMpper.put(variable.getName(), uri);
				}
				
				
			} else if(Functor.class.isInstance(entry)) {
				//Function handling case
				
				Functor functor = (Functor) entry;
				JSONArray args = new JSONArray();
				String type = functor.getImplementor().toString();
				
				if(!(type.equalsIgnoreCase("EQ") || type.equalsIgnoreCase("NE") ||
						type.equalsIgnoreCase("GE") || type.equalsIgnoreCase("LE") ||
							type.equalsIgnoreCase("GT") || type.equalsIgnoreCase("LT") || 
								type.equalsIgnoreCase("and") || type.equalsIgnoreCase("or") || type.equalsIgnoreCase("not")))  continue;

				
				for(Node arg: functor.getArgs())
					if(arg.isVariable()) {
						Node_RuleVariable variable = (Node_RuleVariable) arg;
												
						if((uri = variableMpper.get(variable.getName())) == null)
							throw new IllegalArgumentException("Unknwon variable name: "+variable.getName());
						
					} else if(arg.isLiteral()) {
						Node_Literal var = (Node_Literal) arg;
						
						args.put(new JSONObject()
										.put("value", var.getLiteralValue())
										.put("xml-type", var.getLiteralDatatypeURI()));
					}
				
				
				JSONObject operand = new JSONObject()
										.put("builtin", type)
										.put("preference", uri)
										.put("args", args);
								
				bodyOperands.put(operand);
			}
		}

		return bodyOperands;
	}
	
	private static String getURI(String predicateURI) {
		
		String uri = null;
		if((uri = OntPreference.getURI(predicateURI)) == null)
			uri = OntUserContext.getURI(predicateURI);
		
		return uri;
	}
	
	private static String getPredicate(String uri) {
		
		String predicate = null;
		if((predicate = OntPreference.getPredicate(uri)) == null)
			predicate = OntUserContext.getPredicate(uri);
		
		return predicate;
	}
	
/*
	public static JSONArray convertAll(List<Rule> rules) {
		
		JSONArray jsonRules = new JSONArray();
		for(Rule rule: rules)
			jsonRules.put(convertAll(rule));

		return jsonRules;
	}
	
	
	public static JSONObject convertAll(Rule rule) {
		
		System.out.println(rule.getName());
		
		JSONObject jsonRule = new JSONObject();
		if(rule.getName() != null) jsonRule.put("name", rule.getName());
		
		JSONArray bodyOperands = convertAll(rule.getBody());
		jsonRule.put("body", bodyOperands);
		
		JSONArray headOperands = convertAll(rule.getHead());
		jsonRule.put("head", headOperands);

		
		return jsonRule;
	}
	
	private static JSONArray convertAll(ClauseEntry[] entries) {
		JSONArray bodyOperands = new JSONArray();

		
		for(ClauseEntry entry : entries) {
			String preference = null;
			JSONObject operand = null;
			
			
			if(TriplePattern.class.isInstance(entry)) {
				//TripelPattern handling case
				
				TriplePattern t = (TriplePattern) entry;
				
				Node_RuleVariable subject = (Node_RuleVariable) t.getSubject();
				Node_URI predicate = (Node_URI) t.getPredicate();
				Node object = t.getObject();
				
				operand = new JSONObject()
						.put("subject", subject.toString())
						.put("predicate", predicate.toString())
						.put("object", object.toString());
				
				
			} else if(Functor.class.isInstance(entry)) {
				//Function handling case
				
				Functor functor = (Functor) entry;
				JSONArray args = new JSONArray();
				String type = functor.getImplementor().getName();
								
				for(Node arg: functor.getArgs()) 
					args.put(arg.toString());
				
				
				operand = new JSONObject()
										.put("builtin", type)
										.put("args", args);	
			}
			
			bodyOperands.put(operand);
		}

		return bodyOperands;
	}
*/
}

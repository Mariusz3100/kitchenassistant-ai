package mariusz.ambroziak.kassistant.ai.utils;

import java.util.ArrayList;
import java.util.List;

import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;

public class ParsingProcessObject {
	private String inputPhrase;
	private NerResults nerResults;
	private TokenizationResults entitylessTokenized;
	private List<QualifiedToken> finalResults;
	
	
	
	
	
	
	
	public List<QualifiedToken> getFinalResults() {
		return finalResults;
	}
	public void setFinalResults(List<QualifiedToken> finalResults) {
		this.finalResults = finalResults;
	}
	public ParsingProcessObject(String inputPhrase) {
		super();
		this.inputPhrase = inputPhrase;
	}
	public String getInputPhrase() {
		return inputPhrase;
	}
	public void setInputPhrase(String inputPhrase) {
		this.inputPhrase = inputPhrase;
	}
	public NerResults getEntities() {
		return nerResults;
	}
	public void setEntities(NerResults entities) {
		this.nerResults = entities;
	}

	
	public List<NamedEntity> getCardinalEntities(){
		NerResults allEntities=this.getEntities();
		List<NamedEntity> retValue=new ArrayList<NamedEntity>();
		if(allEntities==null||allEntities.getEntities()==null||allEntities.getEntities().isEmpty()) {
			return new ArrayList<NamedEntity>();
		}else {
			for(NamedEntity ner:allEntities.getEntities()) {
				if(ner.getLabel().equals(PythonSpacyLabels.entitiesCardinalLabel)) {
					retValue.add(ner);
				}
			}
		}
		return retValue;
	}
	
	public NerResults getNerResults() {
		return nerResults;
	}
	public void setNerResults(NerResults nerResults) {
		this.nerResults = nerResults;
	}
	public TokenizationResults getEntitylessTokenized() {
		return entitylessTokenized;
	}
	public void setEntitylessTokenized(TokenizationResults entitylessTokenized) {
		this.entitylessTokenized = entitylessTokenized;
	}
	public String getEntitylessString(){
		NerResults allEntities=this.getEntities();
		String retValue=inputPhrase;
		if(allEntities==null||allEntities.getEntities()==null||allEntities.getEntities().isEmpty()) {
			return this.getInputPhrase();
		}else {
			for(NamedEntity ner:allEntities.getEntities()) {
				if(ner.getLabel().equals(PythonSpacyLabels.entitiesCardinalLabel)) {
					
				}else {
					retValue=retValue.replaceAll(ner.getText(),"");
				}
			}
			retValue=retValue.replaceAll("( )+", " ");
			return retValue;
		}
	}
	
	
}
package mariusz.ambroziak.kassistant.ai.utils;

import java.util.ArrayList;
import java.util.List;

import mariusz.ambroziak.kassistant.ai.edamam.nlp.LearningTuple;
import mariusz.ambroziak.kassistant.ai.logic.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;

public abstract class AbstractParsingObject {

	private NerResults nerResults;
	private TokenizationResults entitylessTokenized;
	private List<QualifiedToken> finalResults;
	private String quantityPhrase;
	private String productPhrase;
	private List<Token> correctedtokens;

	public List<Token> getCorrectedtokens() {
		return correctedtokens;
	}

	public void setCorrectedtokens(List<Token> correctedtokens) {
		this.correctedtokens = correctedtokens;
	}

	public String getQuantityPhrase() {
		return quantityPhrase;
	}

	public void setQuantityPhrase(String quantityPhrase) {
		this.quantityPhrase = quantityPhrase;
	}

	public String getProductPhrase() {
		return productPhrase;
	}

	public void setProductPhrase(String productPhrase) {
		this.productPhrase = productPhrase;
	}

	public List<QualifiedToken> getFinalResults() {
		return finalResults;
	}

	public void setFinalResults(List<QualifiedToken> finalResults) {
		this.finalResults = finalResults;
	}

	public NerResults getEntities() {
		return nerResults;
	}

	public void setEntities(NerResults entities) {
		this.nerResults = entities;
	}

	public List<NamedEntity> getCardinalEntities() {
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

	public String getEntitylessString() {
		NerResults allEntities=this.getEntities();
		String retValue=this.getOriginalPhrase();
		if(allEntities==null||allEntities.getEntities()==null||allEntities.getEntities().isEmpty()) {
			return retValue;
		}else {
			for(NamedEntity ner:allEntities.getEntities()) {
				if(ner.getLabel().equals(PythonSpacyLabels.entitiesCardinalLabel)) {
					
				}else {
					retValue=retValue.replaceAll(ner.getText(),"").replaceAll("  ", " ").trim();
				}
			}
			retValue=retValue.replaceAll("( )+", " ");
			return retValue;
		}
	}

	protected abstract String getOriginalPhrase();

	public String createCorrectedPhrase() {
		
		return this.getQuantityPhrase()+" of "+this.getProductPhrase();
	}

	public LearningTuple calculateResultFromCollectedData() {
		LearningTuple retValue=new LearningTuple(productPhrase, 0, productPhrase, productPhrase, null);
		
		return retValue;
	}

}

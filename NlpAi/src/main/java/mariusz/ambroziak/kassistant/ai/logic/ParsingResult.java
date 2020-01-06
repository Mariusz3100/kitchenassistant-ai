package mariusz.ambroziak.kassistant.ai.logic;

import java.util.List;

import mariusz.ambroziak.kassistant.ai.edamam.nlp.CalculatedResults;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.LearningTuple;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
//class mainly for presenting in angular
public class ParsingResult{
	private String originalPhrase;
	private LearningTuple expectedResult;
	private CalculatedResults calculatedResult;
	private String entities;
	private String entityLess;
	private String tokenString;
	private List<QualifiedToken> tokens;
	
	private String correctedPhrase;
	private List<Token> correctedTokens;

	private List<List<Token>> originalConnotations;
	public List<List<Token>> getOriginalConnotations() {
		return originalConnotations;
	}

	public void setOriginalConnotations(List<List<Token>> originalConnotations) {
		this.originalConnotations = originalConnotations;
	}

	public List<List<Token>> getCorrectedConnotations() {
		return correctedConnotations;
	}

	public void setCorrectedConnotations(List<List<Token>> correctedConnotations) {
		this.correctedConnotations = correctedConnotations;
	}

	private List<List<Token>> correctedConnotations;
	




	public CalculatedResults getCalculatedResult() {
		return calculatedResult;
	}

	public void setCalculatedResult(CalculatedResults calculatedResult) {
		this.calculatedResult = calculatedResult;
	}

	public String getCorrectedPhrase() {
		return correctedPhrase;
	}

	public void setCorrectedPhrase(String correctedPhrase) {
		this.correctedPhrase = correctedPhrase;
	}

	public List<Token> getCorrectedTokens() {
		return correctedTokens;
	}

	public void setCorrectedTokens(List<Token> correctedtokens) {
		this.correctedTokens = correctedtokens;
	}

	public List<QualifiedToken> getTokens() {
		return tokens;
	}

	public void setTokens(List<QualifiedToken> tokens) {
		this.tokens = tokens;
	}

	public String getTokenString() {
		return tokenString;
	}

	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}

	public String getEntities() {
		return entities;
	}

	public void setEntities(String entities) {
		this.entities = entities;
	}

	public String getEntityLess() {
		return entityLess;
	}

	public void setEntityLess(String entityLess) {
		this.entityLess = entityLess;
	}

	public String getOriginalPhrase() {
		return originalPhrase;
	}

	public void setOriginalPhrase(String originalPhrase) {
		this.originalPhrase = originalPhrase;
	}
	public LearningTuple getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(LearningTuple expectedResult) {
		this.expectedResult = expectedResult;
	}
	
	
}
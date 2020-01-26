package mariusz.ambroziak.kassistant.ai.logic;

import java.util.List;

import mariusz.ambroziak.kassistant.ai.edamam.nlp.CalculatedResults;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.LearningTuple;
import mariusz.ambroziak.kassistant.ai.enums.ProductType;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
//class mainly for presenting in angular
public class ParsingResult{
	private String originalPhrase;
	private LearningTuple expectedResult;
	private CalculatedResults restrictivelyCalculatedResult;
	private CalculatedResults permisivelyCalculatedResult;

	private String entities;
	private String entityLess;
	private String tokenString;
	private List<QualifiedToken> tokens;
	
	private String correctedPhrase;
	private List<Token> correctedTokens;
	private String productTypeFound;
	public String getProductTypeFound() {
		return productTypeFound;
	}

	public void setProductTypeFound(String productTypeFound) {
		this.productTypeFound = productTypeFound;
	}

	private List<ConnectionEntry> originalConnotations;
	public List<ConnectionEntry> getOriginalConnotations() {
		return originalConnotations;
	}

	public void setOriginalConnotations(List<ConnectionEntry> originalConnotations) {
		this.originalConnotations = originalConnotations;
	}

	public List<ConnectionEntry> getCorrectedConnotations() {
		return correctedConnotations;
	}

	public void setCorrectedConnotations(List<ConnectionEntry> correctedConnotations) {
		this.correctedConnotations = correctedConnotations;
	}

	private List<ConnectionEntry> correctedConnotations;
	


	public CalculatedResults getRestrictivelyCalculatedResult() {
		return restrictivelyCalculatedResult;
	}

	public void setRestrictivelyCalculatedResult(CalculatedResults restrictivelyCalculatedResult) {
		this.restrictivelyCalculatedResult = restrictivelyCalculatedResult;
	}

	public CalculatedResults getPermisivelyCalculatedResult() {
		return permisivelyCalculatedResult;
	}

	public void setPermisivelyCalculatedResult(CalculatedResults permisivelyCalculatedResult) {
		this.permisivelyCalculatedResult = permisivelyCalculatedResult;
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
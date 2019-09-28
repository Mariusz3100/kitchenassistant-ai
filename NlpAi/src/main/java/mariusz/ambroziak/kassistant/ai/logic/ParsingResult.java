package mariusz.ambroziak.kassistant.ai.logic;

import java.util.List;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
//class mainly for presenting in angular
class ParsingResult{
	private String originalPhrase;
	private String entities;
	private String entityLess;
	private String tokenString;
	private List<QualifiedToken> tokens;
	

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
	
	
	
}
package mariusz.ambroziak.kassistant.ai.logic;

import mariusz.ambroziak.kassistant.ai.enums.WordType;

class ParsingResult{
	private String originalPhrase;
	private String entities;
	private String entityLess;
	private String tokenString;
	
	

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
package mariusz.ambroziak.kassistant.ai.logic;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;

public class QualifiedToken extends Token {
	private WordType wordType;
	
	public QualifiedToken(Token originalToken,WordType wordType) {
		super(originalToken.getText(),originalToken.getLemma(), originalToken.getTag());
		setWordType(wordType);
	}


	public WordType getWordType() {
		return wordType;
	}

	public void setWordType(WordType wordType) {
		this.wordType = wordType;
	}

}

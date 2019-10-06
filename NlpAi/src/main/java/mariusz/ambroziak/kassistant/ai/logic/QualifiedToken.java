package mariusz.ambroziak.kassistant.ai.logic;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;

public class QualifiedToken extends Token {
	private WordType wordType;


	public QualifiedToken(String text, String lemma, String tag,WordType wordType) {
		super(text, lemma, tag);
		this.wordType=wordType;
	}


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




	@Override
	public String toString() {
		return "Token [text=" + super.getText() + ", lemma=" + super.getLemma() + ", tag=" + super.getTag()+ ", wordType=" + wordType + "]";
	}

	public static QualifiedToken createMerged() {
		return new QualifiedToken("", "", "", WordType.Merged);
	}
	
	
}

package mariusz.ambroziak.kassistant.ai.logic;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;

public class QualifiedToken extends Token {
	private WordType wordType;



	public QualifiedToken(String text, String lemma, String tag, WordType wordType) {
		super(text, lemma, tag);
		this.wordType = wordType;

	}


	public QualifiedToken(Token originalToken,WordType wordType,String relationToHead,
			String relationType) {
		super(originalToken.getText(),originalToken.getLemma(), originalToken.getTag());
		setWordType(wordType);

	}
	
	public QualifiedToken(Token originalToken,WordType wordType) {
		super(originalToken.getText(),originalToken.getLemma(), originalToken.getTag());
		setWordType(wordType);
		setHead(originalToken.getHead());
		setRelationToParentType(originalToken.getRelationToParentType());
		setPos(originalToken.getPos());

	}


	public QualifiedToken(String text, String lemma, String tag, WordType wordType, WordType productelement, String relationToHead,
			String relationType) {
		super(text, lemma, tag);

	}


	private QualifiedToken(String text, String lemma, String tag, WordType wordType,
			String relationToParentType, String pos,String parent) {
		super(text, lemma, tag);
		setWordType(wordType);
		setHead(parent);
		setRelationToParentType(relationToParentType);
	}



	public WordType getWordType() {
		return wordType;
	}

	public void setWordType(WordType wordType) {
		this.wordType = wordType;
	}




	@Override
	public String toString() {
		return "Token [text=" + getText() + ", lemma=" + getLemma() + ", tag=" + getTag() + ", relationToParentType="
				+ getRelationToParentType() + ", pos=" + getPos() + ", head=" + getHead() + ", wordType=" + this.wordType + "]";
	}

	public static QualifiedToken createEmptyElementAfterMerged() {
		return new QualifiedToken("", "", "", WordType.Merged,"","","");
	}
	
	public static QualifiedToken createMerged(String fused,WordType type) {
		return new QualifiedToken(fused, "fused", "fused", type,"fused","fused","fused");
	}
	
	
}

package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenizationResults {
	
	private String phrase;
	
	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	private List<Token> tokens;

	public List<Token> getTokens() {
		return tokens;
	}



	@Override
	public String toString() {
		return "TokenizationResults [phrase=" + phrase + ", tokens=" + tokens + "]";
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

}
@JsonIgnoreProperties(ignoreUnknown = true)
class Token {
	public String text;
	public String lemma;
	public String tag;
	
	@Override
	public String toString() {
		return "Token [text=" + text + ", lemma=" + lemma + ", tag=" + tag + "]";
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}


	



}

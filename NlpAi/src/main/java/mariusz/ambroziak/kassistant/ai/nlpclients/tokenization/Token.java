package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
	private String text;
	private String lemma;
	private String tag;

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Token))
			return false;
		else {
			if(this.text==null&&((Token)obj).getText()!=null)
				return false;
			else 
				return this.getText().equals(((Token)obj).getText());
		}
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.getText()==null?0:this.getText().hashCode();
	}
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
	public Token(String text, String lemma, String tag) {
		super();
		this.text = text;
		this.lemma = lemma;
		this.tag = tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}






}
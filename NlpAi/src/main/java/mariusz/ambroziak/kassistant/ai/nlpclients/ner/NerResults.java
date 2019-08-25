package mariusz.ambroziak.kassistant.ai.nlpclients.ner;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NerResults {
	private String phrase;
	private List<NamedEntity> entities;
	
	@Override
	public String toString() {
		return "NerResults [phrase=" + phrase + ", entities=" + entities + "]";
	}
	public String getPhrase() {
		return phrase;
	}
	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	public List<NamedEntity> getEntities() {
		return entities;
	}
	public void setEntities(List<NamedEntity> entities) {
		this.entities = entities;
	}
	
}

@JsonIgnoreProperties(ignoreUnknown = true)
class NamedEntity{
	private String text;
	private String label;
	private int start;
	private int end;

	public String getText() {
		return text;
	}
	@Override
	public String toString() {
		return "NamedEntity [text=" + text + ", label=" + label + ", start=" + start + ", end=" + end + "]";
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	
}
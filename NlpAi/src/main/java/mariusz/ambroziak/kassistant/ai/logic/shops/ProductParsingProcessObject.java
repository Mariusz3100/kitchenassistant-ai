package mariusz.ambroziak.kassistant.ai.logic.shops;

import java.util.ArrayList;
import java.util.List;

import mariusz.ambroziak.kassistant.ai.edamam.nlp.LearningTuple;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.PythonSpacyLabels;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.ai.utils.AbstractParsingObject;

public class ProductParsingProcessObject extends AbstractParsingObject{
	private Tesco_Product product;

	public ProductParsingProcessObject(Tesco_Product product) {
		super();
		this.product = product;
	}

	public Tesco_Product getProduct() {
		return product;
	}

	public void setProduct(Tesco_Product product) {
		this.product = product;
	}

	@Override
	protected String getOriginalPhrase() {
		return this.getProduct().getName();
	}

	
	
}

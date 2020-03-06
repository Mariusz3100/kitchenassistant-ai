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
import mariusz.ambroziak.kassistant.ai.tesco.ProductData;
import mariusz.ambroziak.kassistant.ai.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.ai.utils.AbstractParsingObject;

public class ProductParsingProcessObject extends AbstractParsingObject{
	private ProductData product;
	private List<String> expectedWords;

	public List<String> getExpectedWords() {
		return expectedWords;
	}

	public void setExpectedWords(List<String> expectedWords) {
		this.expectedWords = expectedWords;
	}



	public ProductData getProduct() {
		return product;
	}

	public void setProduct(ProductData product) {
		this.product = product;
	}

	public ProductParsingProcessObject(Tesco_Product product) {
		super();
		this.product = product;
	}



	@Override
	protected String getOriginalPhrase() {
		return this.getProduct().getName();
	}

	
	
}

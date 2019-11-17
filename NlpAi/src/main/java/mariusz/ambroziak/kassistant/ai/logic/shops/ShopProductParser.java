package mariusz.ambroziak.kassistant.ai.logic.shops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import ch.qos.logback.core.pattern.SpacePadder;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.CalculatedResults;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.LearningTuple;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResult;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.logic.WordClasifier;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.tesco.TescoApiClientService;
import mariusz.ambroziak.kassistant.ai.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.ai.utils.ParsingProcessObject;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiResult;



@Service
public class ShopProductParser {
	@Autowired
	private TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	@Autowired
	private TescoApiClientService tescoService;
	@Autowired
	private EdamanIngredientParsingService edamanNlpParsingService;
	@Autowired
	private WordClasifier wordClasifier;

	
	
	
	
	private String spacelessRegex="(\\d+)(\\w+)";
	
	








	public ParsingResultList categorizeProducts(String phrase) throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<Tesco_Product> inputs= tescoService.getProduktsFor(phrase);
		for(Tesco_Product product:inputs) {
			ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);

			String name=product.getName();

			
			NerResults entitiesFound = this.nerRecognizer.find(name);
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());

//			this.wordClasifier.calculateWordsType(parsingAPhrase);


			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;

	}




	private ParsingResult createResultObject(ProductParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
		String fused=parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );

		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());

		
		return object;
	}




	private ParsingResult createResultObject(ParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getLearningTuple().getOriginalPhrase());
		object.setTokens(parsingAPhrase.getFinalResults());
		
		String fused=parsingAPhrase.getCardinalEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );

		
		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		object.setCorrectedPhrase(parsingAPhrase.createCorrectedPhrase());
		object.setCorrectedTokens(parsingAPhrase.getCorrectedtokens());
		object.setExpectedResult(parsingAPhrase.getLearningTuple());
		object.setCalculatedResult(calculateWordsFound(parsingAPhrase));
		return object;
	}




	private CalculatedResults calculateWordsFound(ParsingProcessObject parsingAPhrase) {
		String expected=parsingAPhrase.getLearningTuple().getFoodMatch();
		
		List<String> found=new ArrayList<String>();
		List<String> mistakenlyFound=new ArrayList<String>();

		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
			if(qt.getWordType()==WordType.ProductElement) {
				if(expected.contains(qt.getText())) {
					found.add(qt.getText());
					expected=expected.replaceAll(qt.getText(), "").replaceAll("  ", " ");
				}else {
					mistakenlyFound.add(qt.getText());
				}
			}
		}
		
		List<String> notFound=Arrays.asList(expected.split(" "));

		return new CalculatedResults(notFound,found,mistakenlyFound);
		
	}










	

	public TokenizationResults tokenizeSingleWord(String phrase) throws WordNotFoundException {
		TokenizationResults parse = this.tokenizator.parse(phrase);

		if(parse.getTokens()==null||parse.getTokens().size()<1) {
			return new TokenizationResults();
		}else {
			return parse;
		}

	}




	public void tescoGetResults(String param) {
		List<Tesco_Product> inputs= tescoService.getProduktsFor(param);
		for(Tesco_Product tp:inputs) {
			System.out.println(tp.getName()+";"+tp.getDetailsUrl());
		}
		
	}


}

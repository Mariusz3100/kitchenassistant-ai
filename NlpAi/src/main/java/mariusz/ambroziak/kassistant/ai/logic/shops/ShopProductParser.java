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
import mariusz.ambroziak.kassistant.ai.logic.ProductsWordsClasifier;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.logic.WordClasifier;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.tesco.TescoApiClientService;
import mariusz.ambroziak.kassistant.ai.tesco.TescoDetailsApiClientService;
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
	private TescoDetailsApiClientService tescoDetailsService;
	@Autowired
	private EdamanIngredientParsingService edamanNlpParsingService;
	@Autowired
	private WordClasifier wordClasifier;
	@Autowired
	private ProductsWordsClasifier shopWordClacifier;
	
	
	
	
	private String spacelessRegex="(\\d+)(\\w+)";
	
	








	public ParsingResultList categorizeProducts(String phrase) throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<Tesco_Product> inputs= this.tescoService.getProduktsFor(phrase);
		for(Tesco_Product product:inputs) {
			ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);
			NerResults entitiesFound = this.nerRecognizer.find(product.getName());
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());

			this.shopWordClacifier.calculateWordsType(parsingAPhrase);


			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;

	}




	private ParsingResult createResultObject(ProductParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getProduct().getName());
		String fused=parsingAPhrase.getEntities().getEntities().stream().map(s->s.getText()).collect( Collectors.joining("<br>") );

		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		object.setTokens(parsingAPhrase.getFinalResults());
		object.setCalculatedResult(calculateWordsFound(parsingAPhrase));
		return object;
	}








	private CalculatedResults calculateWordsFound(ProductParsingProcessObject parsingAPhrase) {
		String expected="";
		
		List<String> found=new ArrayList<String>();
		List<String> mistakenlyFound=new ArrayList<String>();
		found=parsingAPhrase.getFinalResults().stream().filter(t->t.getWordType()==WordType.ProductElement).map(t->t.getText()).collect(Collectors.toList());
		
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




	public ParsingResultList parseFromFile() throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<Tesco_Product> inputs= tescoDetailsService.getProduktsFromFile();
		for(Tesco_Product product:inputs) {
			ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);

			String name=product.getName();

			
			NerResults entitiesFound = this.nerRecognizer.find(name);
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());

			this.wordClasifier.calculateWordsType(parsingAPhrase);


			ParsingResult singleResult = createResultObject(parsingAPhrase);
			
			retValue.addResult(singleResult);

			
		}

		return retValue;
	}


}

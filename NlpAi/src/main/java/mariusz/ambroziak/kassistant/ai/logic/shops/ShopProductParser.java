package mariusz.ambroziak.kassistant.ai.logic.shops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.ai.logic.CalculatedResults;
import mariusz.ambroziak.kassistant.ai.edamam.nlp.EdamanIngredientParsingService;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResult;
import mariusz.ambroziak.kassistant.ai.logic.ParsingResultList;
import mariusz.ambroziak.kassistant.ai.logic.ProductsWordsClasifier;
import mariusz.ambroziak.kassistant.ai.logic.QualifiedToken;
import mariusz.ambroziak.kassistant.ai.logic.WordClasifier;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.tesco.TescoApiClientService;
import mariusz.ambroziak.kassistant.ai.tesco.TescoDetailsApiClientService;
import mariusz.ambroziak.kassistant.ai.tesco.Tesco_Product;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordNotFoundException;


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
		for(int i=0;i<inputs.size()&&i<5;i++) {
			Tesco_Product product=inputs.get(i);
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

		String expected=parsingAPhrase.getExpectedWords().stream().collect(Collectors.joining(" ")).toLowerCase();
		object.setRestrictivelyCalculatedResult(calculateWordsFound(expected,parsingAPhrase.getFinalResults()));
		object.setPermisivelyCalculatedResult(calculateWordsFound(expected,parsingAPhrase.getPermissiveFinalResults()));

		return object;
	}


	private CalculatedResults calculateWordsFound(String expected,List<QualifiedToken> finalResults) {
		List<String> found=new ArrayList<String>();
		List<String> mistakenlyFound=new ArrayList<String>();

		for(QualifiedToken qt:finalResults) {
			String qtText = qt.getText().toLowerCase();
			if(qt.getWordType()==WordType.ProductElement) {
				if(expected.contains(qtText)) {
					found.add(qtText);
					expected=expected.replaceAll(qtText, "").replaceAll("  ", " ");
				}else {
					mistakenlyFound.add(qtText);
				}
			}
		}

		List<String> notFound=Arrays.asList(expected.split(" "));
		List<String> wordsMarked=finalResults.stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());

		return new CalculatedResults(notFound,found,mistakenlyFound,wordsMarked);
	}

//	private CalculatedResults calculateWordsFound(ProductParsingProcessObject parsingAPhrase) {
//		List<String> found=new ArrayList<String>();
//		List<String> mistakenlyFound=new ArrayList<String>();
//		String expected=parsingAPhrase.getExpectedWords().stream().collect(Collectors.joining(" ")).toLowerCase();
//		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
//			if(qt.getWordType()==WordType.ProductElement) {
//				if(expected.contains(qt.getText().toLowerCase())) {
//					found.add(qt.getText());
//					expected=expected.replaceAll(qt.getText().toLowerCase(), "").replaceAll("  ", " ");
//				}else {
//					mistakenlyFound.add(qt.getText());
//				}
//			}
//		}
//
//		List<String> notFound=Arrays.asList(expected.split(" "));
//		List<String> wordsMarked=parsingAPhrase.getFinalResults().stream().filter(qualifiedToken -> qualifiedToken.getWordType()==WordType.ProductElement).map(qualifiedToken -> qualifiedToken.getText()).collect(Collectors.toList());
//
//		return new CalculatedResults(notFound,found,mistakenlyFound,wordsMarked);
//
//	}










	

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

		List<ProductParsingProcessObject> inputs= tescoDetailsService.getProduktsFromFile();
		for(ProductParsingProcessObject parsingAPhrase:inputs) {
			//ProductParsingProcessObject parsingAPhrase=new ProductParsingProcessObject(product);
			//parsingAPhrase.setProduct(product);
			String originalPhrase=parsingAPhrase.getProduct().getName();


			NerResults entitiesFound = this.nerRecognizer.find(originalPhrase);
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

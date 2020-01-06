package mariusz.ambroziak.kassistant.ai.logic.ingredients;

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
import mariusz.ambroziak.kassistant.ai.utils.ParsingProcessObject;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiResult;



@Service
public class IngredientPhraseParser {
	private TokenizationClientService tokenizator;
	@Autowired
	private NamedEntityRecognitionClientService nerRecognizer;
	private ResourceLoader resourceLoader;

	private EdamanIngredientParsingService edamanNlpParsingService;
	@Autowired
	private WordClasifier wordClasifier;





	private String spacelessRegex="(\\d+)(\\w+)";





	public IngredientPhraseParser(TokenizationClientService tokenizator,
			NamedEntityRecognitionClientService nerRecognizer, ResourceLoader resourceLoader,
			EdamanIngredientParsingService edamanNlpParsingService,
			WordClasifier wordClasifier) {
		super();
		this.tokenizator = tokenizator;
		this.nerRecognizer = nerRecognizer;
		this.resourceLoader = resourceLoader;
		this.edamanNlpParsingService = edamanNlpParsingService;
		this.wordClasifier = wordClasifier;
	}




	public ParsingResultList parseFromFile() throws IOException {
		ParsingResultList retValue=new ParsingResultList();

		List<LearningTuple> inputLines= edamanNlpParsingService.retrieveDataFromFile();
		for(LearningTuple er:inputLines) {
			String line=correctErrors(er.getOriginalPhrase());

			ParsingProcessObject parsingAPhrase=new ParsingProcessObject(er);

			NerResults entitiesFound = this.nerRecognizer.find(line);
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




	private ParsingResult createResultObject(ParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getLearningTuple().getOriginalPhrase());
		List<QualifiedToken> primaryResults = parsingAPhrase.getFinalResults();
		object.setTokens(primaryResults);

		String fused=parsingAPhrase.getCardinalEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );


		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		object.setCorrectedPhrase(parsingAPhrase.createCorrectedPhrase());
		object.setCorrectedTokens(parsingAPhrase.getCorrectedtokens());
		object.setExpectedResult(parsingAPhrase.getLearningTuple());
		object.setCalculatedResult(calculateWordsFound(parsingAPhrase));


		setConnotations(parsingAPhrase, object, primaryResults);

		return object;
	}




	private void setConnotations(ParsingProcessObject parsingAPhrase, ParsingResult object,
			List<QualifiedToken> primaryResults) {
		List<Token> correctedTokens =parsingAPhrase.getCorrectedtokens();
		List<List<Token>> originalConotations = extractPrimarilyFoundConnotations(primaryResults);

		List<List<Token>> correctedConotations = findConnotationsInCorrected(correctedTokens);

		object.setOriginalConnotations(originalConotations);
		object.setCorrectedConnotations(correctedConotations);
	}




	private List<List<Token>> extractPrimarilyFoundConnotations(List<QualifiedToken> primaryResults) {
		List<List<Token>> originalConotations=new ArrayList<List<Token>>();

		for(int i=0;i<primaryResults.size();i++) {
			List<Token> entryFromPrimary=new ArrayList<Token>();
			QualifiedToken qt=primaryResults.get(i);
			if(qt.getWordType()!=WordType.QuantityElement) {
				entryFromPrimary.add(primaryResults.get(i));

				QualifiedToken tokenFromFinal=(QualifiedToken) findHeadInTokens(primaryResults, primaryResults.get(i));
				if(tokenFromFinal!=null&&tokenFromFinal.getWordType()!=WordType.QuantityElement)
					entryFromPrimary.add(tokenFromFinal);

				if(entryFromPrimary.size()>=2) {
					originalConotations.add(entryFromPrimary);
				}

			}


		}
		return originalConotations;
	}




	private List<List<Token>> findConnotationsInCorrected(List<Token> correctedTokens) {
		List<List<Token>> correctedConotations=new ArrayList<List<Token>>();
		boolean ofPassed=false;

		for(Token tokenFromCorrected:correctedTokens) {

			if("of".equals(tokenFromCorrected.getText())) {
				ofPassed=true;
			}else if(ofPassed){

				List<Token> correctedEntry=new ArrayList<Token>();
				correctedEntry.add(tokenFromCorrected);
				Token found=findHeadInTokens(correctedTokens, tokenFromCorrected);
				if(found!=null)
					correctedEntry.add(found);

				if(correctedEntry.size()>=2) {
					correctedConotations.add(correctedEntry);
				}
			}
		}
		return correctedConotations;
	}




	private Token findHeadInTokens(List<? extends Token> correctedTokens, Token getHeadFotThis) {
		for(Token t:correctedTokens) {
			if(t.getText().equals(getHeadFotThis.getHead())&&!t.getText().equals(getHeadFotThis.getText())) {
				return t;
			}
		}
		return null;
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




	private String createEntitiesString(NerResults entitiesFound) {
		String entitiesString="";

		if(entitiesFound!=null&&!entitiesFound.getEntities().isEmpty()) {
			for(NamedEntity ner:entitiesFound.getEntities()) {
				entitiesString+=ner.getText()+" ["+ner.getLabel()+"]";
			}
		}
		return entitiesString;
	}






	private String correctErrors(String phrase) {

		phrase=phrase.replaceFirst("½", "1/2");
		phrase=phrase.replaceFirst("¼", "1/4");

		String replacedString=phrase.replaceAll(spacelessRegex, "$1 $2");
		if(!phrase.equals(replacedString)) {
			phrase=replacedString;
		}

		if(phrase.substring(0, phrase.length()<10?phrase.length():10).indexOf(" c ")>0) {
			phrase=phrase.replaceFirst(" c ", " cup ");
		}

		if(phrase.substring(0, phrase.length()<10?phrase.length():10).indexOf(" & ")>0) {
			phrase=phrase.replaceFirst(" & ", " and ");
		}

		return phrase;
	}



	public TokenizationResults tokenizeSingleWord(String phrase) throws WordNotFoundException {
		TokenizationResults parse = this.tokenizator.parse(phrase);

		if(parse.getTokens()==null||parse.getTokens().size()<1) {
			return new TokenizationResults();
		}else {
			return parse;
		}

	}


}

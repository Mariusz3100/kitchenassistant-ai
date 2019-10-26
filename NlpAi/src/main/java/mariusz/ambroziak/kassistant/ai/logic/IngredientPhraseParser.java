package mariusz.ambroziak.kassistant.ai.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import mariusz.ambroziak.kassistant.ai.enums.WordType;
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
	private Resource inputFileResource;

	@Autowired
	private WordClasifier wordClasifier;

	
	
	
	
	private String spacelessRegex="(\\d+)(\\w+)";
	
	public IngredientPhraseParser(TokenizationClientService tokenizator,
			NamedEntityRecognitionClientService nerRecognizer, ResourceLoader resourceLoader) {
		super();
		this.tokenizator = tokenizator;
		this.nerRecognizer = nerRecognizer;
		this.resourceLoader = resourceLoader;
		this.inputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
	}




	public ParsingResultList parseFromFile() throws IOException {
		ParsingResultList retValue=new ParsingResultList();
		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();

		while(line!=null) {
			line=correctErrors(line);



			ParsingProcessObject parsingAPhrase=new ParsingProcessObject(line);
			NerResults entitiesFound = this.nerRecognizer.find(line);
			parsingAPhrase.setEntities(entitiesFound);

			String entitylessString=parsingAPhrase.getEntitylessString();

			TokenizationResults tokens = this.tokenizator.parse(entitylessString);
			parsingAPhrase.setEntitylessTokenized(tokens);
			
			parsingAPhrase.setFinalResults(new ArrayList<QualifiedToken>());
			

			this.wordClasifier.calculateWordsType(parsingAPhrase);


			ParsingResult singleResult = createResultObject(parsingAPhrase);

			retValue.addResult(singleResult);

			line=br.readLine();
		}

		return retValue;

	}




	private ParsingResult createResultObject(ParsingProcessObject parsingAPhrase) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(parsingAPhrase.getInputPhrase());
		object.setTokens(parsingAPhrase.getFinalResults());
		
		String fused=parsingAPhrase.getCardinalEntities().stream().map(s->s.getText()).collect( Collectors.joining(" ") );

		
		object.setEntities(fused);
		object.setEntityLess(parsingAPhrase.getEntitylessString());
		return object;
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




	private ParsingResult createResultObject(String line, String entitiesString, String entitylessString,
			List<QualifiedToken> results) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(line);
		object.setTokens(results);
		object.setEntities(entitiesString);
		object.setEntityLess(entitylessString);
		return object;
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

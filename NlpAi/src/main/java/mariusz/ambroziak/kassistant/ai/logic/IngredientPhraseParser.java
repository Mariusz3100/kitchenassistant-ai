package mariusz.ambroziak.kassistant.ai.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
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
		MultiValuedMap<String, NamedEntity> entitiesMap = new ArrayListValuedHashMap<>();

		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));


		String line=br.readLine();

		while(line!=null) {
			line=correctErrors(line);


			NerResults entitiesFound = this.nerRecognizer.find(line);
			String entitiesString="";
			String entitylessString="";

			if(entitiesFound!=null&&!entitiesFound.getEntities().isEmpty()) {
				for(NamedEntity ner:entitiesFound.getEntities()) {
					entitiesString+=ner.getText()+" ["+ner.getLabel()+"]";
					if(ner.getLabel().equals("CARDINAL")) {
						entitiesMap.put(line, ner);
					}else {
						entitylessString+=line.replaceAll(ner.getText(),"");
					}
				}
			}
			if(entitylessString.isEmpty()) {
				entitylessString=line;
			}

			List<QualifiedToken> results=calculateWordType(entitylessString,entitiesMap);

			String tokensString="| ";
			for(QualifiedToken t:results){

				tokensString+=t.text+" | ";
			}
			ParsingResult object = createResultObject(line, entitiesString, entitylessString, results, tokensString);

			retValue.addResult(object);

			line=br.readLine();
		}

		return retValue;

	}




	private ParsingResult createResultObject(String line, String entitiesString, String entitylessString,
			List<QualifiedToken> results, String tokensString) {
		ParsingResult object=new ParsingResult();
		object.setOriginalPhrase(line);
		object.setTokens(results);
		object.setEntities(entitiesString);
		object.setEntityLess(entitylessString);
		object.setTokenString(tokensString);
		return object;
	}


	private static String correctErrors(String phrase) {

		phrase=phrase.replaceFirst("½", "1/2");
		phrase=phrase.replaceFirst("¼", "1/4");
		if(phrase.substring(0, phrase.length()<10?phrase.length():10).indexOf(" c ")>0) {
			phrase=phrase.replaceFirst(" c ", " cup ");
		}



		return phrase;
	}


	private List<QualifiedToken> calculateWordType(String phrase, MultiValuedMap<String, NamedEntity> entitiesMap) {
		List<String> tokens = this.tokenizator.justTokenize(phrase);

		List<QualifiedToken> retValue=new ArrayList<QualifiedToken>();

		for(String t:tokens) {

			WordType chosenType = null;


			try {
				Token tempToken = tokenizeSingleWord(t);

				chosenType = this.wordClasifier.classifyWord(tempToken);
				retValue.add(new QualifiedToken(tempToken, chosenType));

			} catch (WordNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Token tempToken=new Token(t, "not found", "not found");
				retValue.add(new QualifiedToken(tempToken,null));

				
			}



		}

		return retValue;

	}
	
	public WordType classifyWord(Token t) throws WordNotFoundException {
		 return this.wordClasifier.classifyWord(t);
	}

//	public WordType classifyWord(String phrase) throws WordNotFoundException {
//		TokenizationResults parse = this.tokenizator.parse(phrase);
//		
//		if(parse.getTokens()==null||parse.getTokens().size()<0) {
//			return null;
//		}else {
//			return this.wordClasifier.classifyWord(parse.getTokens().get(0));
//		}
//		
//	}

	public Token tokenizeSingleWord(String phrase) throws WordNotFoundException {
		TokenizationResults parse = this.tokenizator.parse(phrase);
		
		if(parse.getTokens()==null||parse.getTokens().size()<0) {
			return null;
		}else {
			return parse.getTokens().get(0);
		}
		
	}


}

package mariusz.ambroziak.kassistant.ai.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.utils.ParsingProcessObject;
import mariusz.ambroziak.kassistant.ai.utils.QuantityTranslation;
import mariusz.ambroziak.kassistant.ai.wikipedia.WikipediaApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.ConvertApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiResult;

@Service
public class WordClasifier {
	private static String wikipediaCheckRegex=".*[a-zA-Z].*";
	private static String convertApiCheckRegex=".*[a-zA-Z].*";
	private static String punctuationRegex="[\\.,\\-]*";


	@Autowired
	private WordsApiClient wordsApiClient;

	@Autowired
	private WikipediaApiClient wikipediaClient;
	@Autowired
	private ConvertApiClient convertClient;


	public static ArrayList<String> productTypeKeywords;
	public static ArrayList<String> irrelevanceKeywords;
	public static ArrayList<String> quantityTypeKeywords;
	public static ArrayList<String> punctationTypeKeywords;



	static {
		productTypeKeywords=new ArrayList<String>();

		productTypeKeywords.add("vegetable");


		productTypeKeywords.add("flavouring");
		productTypeKeywords.add("seasoning");
		productTypeKeywords.add("dairy");
		productTypeKeywords.add("meat");
		productTypeKeywords.add("food");





		irrelevanceKeywords=new ArrayList<String>();
		irrelevanceKeywords.add("activity");
		irrelevanceKeywords.add("love");


		quantityTypeKeywords=new ArrayList<String>();
		quantityTypeKeywords.add("containerful");
		quantityTypeKeywords.add("small indefinite quantity");
		quantityTypeKeywords.add("weight unit");
		quantityTypeKeywords.add("capacity unit");






	}

	//	public WordType classifyWord(Token t) throws WordNotFoundException {
	//		TokenizationResults empty=new TokenizationResults();
	//		empty.setPhrase("");
	//		empty.setTokens(new ArrayList<Token>());
	//		return classifyWord(empty , t);
	//	
	//	}

	public List<QualifiedToken> calculateWordsType(ParsingProcessObject parsingAPhrase) {
		Map<String,QualifiedToken> wordToResultMap=new HashMap<String,QualifiedToken>();
		parsingAPhrase.setIndex(0);

		//for(int i=0;i<parsingAPhrase.getEntitylessTokenized().getTokens().size();i++) {
		while(!parsingAPhrase.isOver()) {
			Token t=parsingAPhrase.getEntitylessTokenized().getTokens().get(parsingAPhrase.getIndex());
			WordType chosenType = null;

			if(PythonSpacyLabels.tokenisationCardinalLabel.equals(t.getTag())) {
				chosenType=WordType.QuantityElement;
				parsingAPhrase.setIndex(parsingAPhrase.getIndex()+1);

				List<NamedEntity> cardinalEntities = parsingAPhrase.getCardinalEntities();
				for(NamedEntity cardinalEntity:cardinalEntities) {
					if(cardinalEntity.getText().contains(t.getText())){
						if(!PythonSpacyLabels.entitiesCardinalLabel.equals(cardinalEntity.getLabel())) {
							System.err.println("Tokenization and Ner labels do not match");
							chosenType=null;

						}
					}
				}
			}else {

				try {
					chosenType = classifyWord(parsingAPhrase,wordToResultMap);

				} catch (WordNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}



			wordToResultMap.put(t.getText(), new QualifiedToken(t, chosenType));
		}

		QualifiedToken[] array = wordToResultMap.values().toArray(new QualifiedToken[]{});

		List<QualifiedToken> asList = Arrays.asList(array);
		parsingAPhrase.setFinalResults(asList);
		return asList;

	}

	public WordType classifyWord(ParsingProcessObject parsingAPhrase, Map<String, QualifiedToken> wordToResultMap) throws WordNotFoundException {

		Token t=parsingAPhrase.getEntitylessTokenized().getTokens().get(parsingAPhrase.getIndex());
		WordType retValue=null;
		String token=t.getText();
		String lemma=t.getLemma();


		if(Pattern.matches(punctuationRegex, token)) {
			return WordType.PunctuationElement;
		}

		ArrayList<WordsApiResult> wordResults = wordsApiClient.searchFor(token);

		if(wordResults==null||wordResults.isEmpty()) {

			if(lemma!=null&&!lemma.isEmpty()&&!lemma.equals("O"))
			{
				wordResults = wordsApiClient.searchFor(lemma);

			}
		}
		if(wordResults==null||wordResults.isEmpty()) {

			if((lemma!=null&&!lemma.isEmpty())&&Pattern.matches(convertApiCheckRegex, lemma))
			{
				QuantityTranslation checkForTranslation = convertClient.checkForTranslation(token);
				if(checkForTranslation!=null) {
					return WordType.QuantityElement;
				}
			}
		}
		if(wordResults==null||wordResults.isEmpty()) {

			String baseWord =null;

			if(Pattern.matches(wikipediaCheckRegex, token)) {
				baseWord=wikipediaClient.getRedirectIfAny(token);
			}

			if((baseWord==null||baseWord.isEmpty())&&Pattern.matches(convertApiCheckRegex, token))
			{
				QuantityTranslation checkForTranslation = convertClient.checkForTranslation(token);
				if(checkForTranslation!=null) {
					return WordType.QuantityElement;
				}
			}
			if(baseWord!=null&&!baseWord.isEmpty())
			{
				wordResults = wordsApiClient.searchFor(baseWord);
			}
		}

		if(wordResults!=null&&!wordResults.isEmpty()) {
			WordsApiResult quantityTypeRecognized = checkQuantityTypesForWordObject(wordResults);
			if(quantityTypeRecognized!=null) {
				parsingAPhrase.setIndex(parsingAPhrase.getIndex()+1);	return WordType.QuantityElement;
			} else {
				WordsApiResult productTypeRecognized = checkProductTypesForWordObject(wordResults);
				if(productTypeRecognized!=null) {

					checkOtherTokens(parsingAPhrase,productTypeRecognized,wordToResultMap);



					return WordType.ProductElement;

				}else {
					parsingAPhrase.setIndex(parsingAPhrase.getIndex()+1);

					return null;
				}
			}
		}




		return retValue;
	}



	private int checkOtherTokens(ParsingProcessObject parsingAPhrasex, WordsApiResult productTypeRecognized,
			Map<String, QualifiedToken> wordToResultMap) {

		TokenizationResults tokens=parsingAPhrasex.getEntitylessTokenized();
		int index=parsingAPhrasex.getIndex();
		List<String> setOfRelevantWords=new ArrayList<String>();

		setOfRelevantWords.addAll(productTypeRecognized.getChildTypes());
		setOfRelevantWords.addAll(productTypeRecognized.getSynonyms());
		setOfRelevantWords.sort(Collections.reverseOrder());
		for(String expandedWordFromApi:setOfRelevantWords) {
			//check if it exists
			if(tokens.getPhrase().indexOf(expandedWordFromApi)>=0){
				//check if it exists in the right place
				int expandedWordFromApiLength=expandedWordFromApi.split(" ").length;
				List<Token> actualTokens = tokens.getTokens();
				//does it start before current index?

				if(expandedWordFromApiLength>1) {
					if(index-expandedWordFromApiLength>=0) {
						List<Token> subList =actualTokens.subList(index-expandedWordFromApiLength, index);
						String fused=subList.stream().map(s->s.getText()).collect( Collectors.joining(" ") );
						if(fused.indexOf(expandedWordFromApi)>=0) {
							QualifiedToken result=new QualifiedToken(fused, "fused", "fused", WordType.ProductElement);
							for(Token t:subList) {
								wordToResultMap.remove(t.getText());

							}

							wordToResultMap.put(fused, result);
							parsingAPhrasex.setIndex(index);
							return index;

						}
						//does it end after current index?
					}else if(expandedWordFromApiLength+index<=actualTokens.size()) {
						List<Token> subList = actualTokens.subList(index, expandedWordFromApiLength+index);
						String fused=subList.stream().map(s->s.getText()).collect( Collectors.joining(" ") );
						if(fused.indexOf(expandedWordFromApi)>=0) {
							QualifiedToken result=new QualifiedToken(fused, "fused", "fused", WordType.ProductElement);
							index+=expandedWordFromApiLength-1;
							wordToResultMap.put(fused, result);
							parsingAPhrasex.setIndex(index);
							return index;

						}
					}else {
						System.err.println("well, we got some word in the middle of sentence case in word api");
					}
				}else {
					parsingAPhrasex.setIndex(parsingAPhrasex.getIndex()+1);

				}
			}

		}

		return index;

	}

	private static WordsApiResult checkProductTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
		for(WordsApiResult war:wordResults) {
			if(checkIfTypesContainKeywords(war.getOriginalWord(),war.getTypeOf(),productTypeKeywords)) {
				return war;
			}
		}
		return null;		
	}


	private static WordsApiResult checkQuantityTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
		for(WordsApiResult war:wordResults) {
			if(checkIfTypesContainKeywords(war.getOriginalWord(),war.getTypeOf(),quantityTypeKeywords)) {
				return war;
			}
		}
		return null;		
	}




	private static boolean checkIfTypesContainKeywords(String productName, ArrayList<String> typeResults,ArrayList<String> keywords) {
		for(String typeToBeChecked:typeResults) {
			for(String typeConsidered:keywords) {
				if(typeToBeChecked.indexOf(typeConsidered)>=0) {
					System.out.println(productName+" -> "+typeToBeChecked+" : "+typeConsidered);

					return true;
				}
			}
		}
		return false;
	}




}

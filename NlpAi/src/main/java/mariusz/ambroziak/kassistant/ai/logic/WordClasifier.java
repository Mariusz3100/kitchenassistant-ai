package mariusz.ambroziak.kassistant.ai.logic;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
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

	
	@Autowired
	private WordsApiClient wordsApiClient;

	@Autowired
	private WikipediaApiClient wikipediaClient;
	@Autowired
	private ConvertApiClient convertClient;


	public static ArrayList<String> productTypeKeywords;
	public static ArrayList<String> irrelevanceKeywords;
	public static ArrayList<String> quantityTypeKeywords;



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



	}


	public WordType classifyWord(Token t) throws WordNotFoundException {

		WordType retValue=null;
		String token=t.getText();
		String lemma=t.getLemma();
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
			if(checkQuantityTypesForWordObject(wordResults)) {
				return WordType.QuantityElement;
			}else if(checkProductTypesForWordObject(wordResults)) {
				return WordType.ProductElement;
			}else {
				return null;
			}
		}




		return retValue;
	}



	private static boolean checkProductTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
		for(WordsApiResult war:wordResults) {
			if(checkIfTypesContainKeywords(war.getOriginalWord(),war.getTypeOf(),productTypeKeywords)) {
				return true;
			}
		}
		return false;		
	}


	private static boolean checkQuantityTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
		for(WordsApiResult war:wordResults) {
			if(checkIfTypesContainKeywords(war.getOriginalWord(),war.getTypeOf(),quantityTypeKeywords)) {
				return true;
			}
		}
		return false;		
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

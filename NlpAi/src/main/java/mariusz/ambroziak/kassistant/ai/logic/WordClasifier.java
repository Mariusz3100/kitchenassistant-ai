package mariusz.ambroziak.kassistant.ai.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mariusz.ambroziak.kassistant.ai.enums.MergeType;
import mariusz.ambroziak.kassistant.ai.enums.ProductType;
import mariusz.ambroziak.kassistant.ai.enums.WordType;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntity;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.ConnectionEntry;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.Token;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;
import mariusz.ambroziak.kassistant.ai.utils.AbstractParsingObject;
import mariusz.ambroziak.kassistant.ai.utils.QuantityTranslation;
import mariusz.ambroziak.kassistant.ai.wikipedia.WikipediaApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.ConvertApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordNotFoundException;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiClient;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiResult;
import mariusz.ambroziak.kassistant.ai.wordsapi.WordsApiResultImpostor;

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
	@Autowired
	private TokenizationClientService tokenizator;


	public static ArrayList<String> productTypeKeywords;
	public static ArrayList<String> irrelevanceKeywords;
	public static ArrayList<String> quantityTypeKeywords;
	public static ArrayList<String> quantityAttributeKeywords;
	public static ArrayList<String> punctationTypeKeywords;

	public static ArrayList<String> freshFoodKeywords;


	static {
		productTypeKeywords=new ArrayList<String>();

		productTypeKeywords.add("vegetable");


		productTypeKeywords.add("flavouring");
		productTypeKeywords.add("seasoning");
		productTypeKeywords.add("dairy");
		productTypeKeywords.add("meat");
		productTypeKeywords.add("food");
		productTypeKeywords.add("sweetener");
		productTypeKeywords.add("cheese");
		productTypeKeywords.add("citrous fruit");



		irrelevanceKeywords=new ArrayList<String>();
		irrelevanceKeywords.add("activity");
		irrelevanceKeywords.add("love");


		quantityTypeKeywords=new ArrayList<String>();
		quantityTypeKeywords.add("containerful");
		quantityTypeKeywords.add("small indefinite quantity");
		quantityTypeKeywords.add("weight unit");
		quantityTypeKeywords.add("capacity unit");


		//presumably too specific ones:
		productTypeKeywords.add("dressing");

		quantityAttributeKeywords=new ArrayList<String>();
		quantityAttributeKeywords.add("size");

		freshFoodKeywords=new ArrayList<String>();
		freshFoodKeywords.add("fresh");

	}


	public void calculateWordsType(AbstractParsingObject parsingAPhrase) {
		initialCategorization(parsingAPhrase);

		recategorize(parsingAPhrase);
		extractAndMarkProductProperties(parsingAPhrase);

		categorizeAllElseAsProducts(parsingAPhrase);

	}

	private void extractAndMarkProductProperties(AbstractParsingObject parsingAPhrase) {
		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
			for(String keyword:freshFoodKeywords) {
				if(keyword.equals(qt.getText())) {
					qt.setWordType(WordType.ProductPropertyElement);
					parsingAPhrase.setFoodTypeClassified(ProductType.fresh);
				}
			}
		}

	}

	private void categorizeAllElseAsProducts(AbstractParsingObject parsingAPhrase) {
		List<QualifiedToken> permissiveList=new ArrayList<QualifiedToken>();
		for(QualifiedToken qt:parsingAPhrase.getFinalResults()) {
			WordType type=qt.getWordType()==null?WordType.ProductElement:qt.getWordType();
			permissiveList.add(new QualifiedToken(qt.getText(), qt.getLemma(), qt.getTag(), type));
		}
		parsingAPhrase.setPermissiveFinalResults(permissiveList);
	}

	private void recategorize(AbstractParsingObject parsingAPhrase) {
		fillQuanAndProdPhrases(parsingAPhrase);

		TokenizationResults correctedPhraseParsed = this.tokenizator.parse(parsingAPhrase.createCorrectedPhrase());

		parsingAPhrase.setCorrectedToknized(correctedPhraseParsed);

		TokenizationResults productPhraseparsed = this.tokenizator.parse(parsingAPhrase.getProductPhrase());

		parsingAPhrase.setProductPhraseTokenized(productPhraseparsed);


	}

	private void fillQuanAndProdPhrases(AbstractParsingObject parsingAPhrase) {
		String quantityPhrase="",productPhrase="";
		for(int i=0;i<parsingAPhrase.getFinalResults().size();i++) {
			QualifiedToken qt=parsingAPhrase.getFinalResults().get(i);
			if(WordType.QuantityElement==qt.getWordType()&&productPhrase.equals("")) {
				quantityPhrase+=qt.getText()+" ";
			}else if(WordType.PunctuationElement==qt.getWordType()) {
				//ignore
			}else {
				productPhrase+=qt.getText()+" ";
			}

		}
		productPhrase=productPhrase.trim();
		quantityPhrase=quantityPhrase.trim();

		parsingAPhrase.setQuantityPhrase(quantityPhrase);
		parsingAPhrase.setProductPhrase(productPhrase);
	}

	private void initialCategorization(AbstractParsingObject parsingAPhrase) {
		for(int i=0;i<parsingAPhrase.getEntitylessTokenized().getTokens().size();i++) {
			Token t=parsingAPhrase.getEntitylessTokenized().getTokens().get(i);

			if(PythonSpacyLabels.tokenisationCardinalLabel.equals(t.getTag())) {
				addQuantityResult(parsingAPhrase,i, t);
				List<NamedEntity> cardinalEntities = parsingAPhrase.getCardinalEntities();

				for(NamedEntity cardinalEntity:cardinalEntities) {
					if(cardinalEntity.getText().contains(t.getText())){
						if(!PythonSpacyLabels.entitiesCardinalLabel.equals(cardinalEntity.getLabel())) {
							System.err.println("Tokenization and Ner labels do not match");
						}
					}
				}
			}else {
				try {
					classifyWord(parsingAPhrase,i);
				} catch (WordNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void classifyWord(AbstractParsingObject parsingAPhrase, int index) throws WordNotFoundException {
		if(parsingAPhrase.getFutureTokens().containsKey(index)) {
			return;
		}

		TokenizationResults tokens=parsingAPhrase.getEntitylessTokenized();
		Token t=tokens.getTokens().get(index);
		String token=t.getText();
		WordType improperlyFoundType=improperlyFindType(parsingAPhrase,index,parsingAPhrase.getFutureTokens());
		if(improperlyFoundType!=null) {
			addResult(parsingAPhrase,index,new QualifiedToken(t,improperlyFoundType));

			return;
		}
		if(Pattern.matches(punctuationRegex, token)) {
			addResult(parsingAPhrase,index,new QualifiedToken(t,WordType.PunctuationElement));

			return;
		}

		ArrayList<WordsApiResult> wordResults = searchForAllPossibleMeaningsInWordsApi(parsingAPhrase, index, t);
		if(wordResults!=null&&!wordResults.isEmpty()) {
			WordsApiResult quantityTypeRecognized = checkQuantityTypesForWordObject(wordResults);
			if(quantityTypeRecognized!=null) {
				addQuantityResult(parsingAPhrase, index, t);
			} else {
				WordsApiResult productTypeRecognized = checkProductTypesForWordObject(wordResults);
				if(productTypeRecognized!=null) {
					checkOtherTokens(parsingAPhrase,index ,productTypeRecognized);
				}else {
					addResult(parsingAPhrase, index, new QualifiedToken(t,null));
				}
			}
		}

	}

	private WordType improperlyFindType(AbstractParsingObject parsingAPhrase, int index,
			Map<Integer, QualifiedToken> map) {
		//TODO this should be deleted in the end
		TokenizationResults tokens=parsingAPhrase.getEntitylessTokenized();
		Token t=tokens.getTokens().get(index);

		if(t.getText().equals("medium"))
			return WordType.QuantityElement;


		return null;
	}

	private ArrayList<WordsApiResult> searchForAllPossibleMeaningsInWordsApi(AbstractParsingObject parsingAPhrase,
			int index, Token t) throws WordNotFoundException {
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
					addQuantityResult(parsingAPhrase,index,t);
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
					WordsApiResult war=new WordsApiResultImpostor(checkForTranslation);
					wordResults=new ArrayList<WordsApiResult>();

					wordResults.add(war);
					return wordResults;
				}
			}
			if(baseWord!=null&&!baseWord.isEmpty())
			{
				wordResults = wordsApiClient.searchFor(baseWord);
			}
		}
		return wordResults;
	}

	protected void addQuantityResult(AbstractParsingObject parsingAPhrase, int index, Token t) {
		QualifiedToken result=new QualifiedToken(t, WordType.QuantityElement);

		addResult(parsingAPhrase,index,result);
	}

	private void addResult(AbstractParsingObject parsingAPhrase, int index, QualifiedToken qt) {

		if(index>=parsingAPhrase.getFinalResults().size())
			parsingAPhrase.getFinalResults().add(qt);
		else
			parsingAPhrase.getFinalResults().set(index,qt);
	}

	private void checkOtherTokens(AbstractParsingObject parsingAPhrase, int index,WordsApiResult productTypeRecognized) {
		if(parsingAPhrase.getFutureTokens().containsKey(index)) {
			return;
		}

		TokenizationResults tokens=parsingAPhrase.getEntitylessTokenized();
		List<String> setOfRelevantWords=new ArrayList<String>();
		//we take them from the longest to the shortest
		setOfRelevantWords.addAll(productTypeRecognized.getChildTypes());
		setOfRelevantWords.addAll(productTypeRecognized.getSynonyms());
		setOfRelevantWords.sort(Collections.reverseOrder());



		boolean extendedWordFound=false;


		for(int i=0;i<setOfRelevantWords.size()&&!extendedWordFound;i++) {
			//check if not longer than 2 words (for now)
			String expandedWordFromApi=setOfRelevantWords.get(i);


			int expandedWordFromApiLength=expandedWordFromApi.split(" ").length;
			if(expandedWordFromApiLength<3)
			{

				List<Token> actualTokens = tokens.getTokens();
				//does it start before current index?

				extendedWordFound=wereAnyTokensMarkedBesideCurrentDueToAdjacency(parsingAPhrase, index, expandedWordFromApi,
						expandedWordFromApiLength, actualTokens);
				if(!extendedWordFound) {
					extendedWordFound=wereAnyTokensReplacedDueToDependencyTree(parsingAPhrase, expandedWordFromApi);
				}


			}
			//if we didn't find compound phrase from words api, it is a single one 
			if(!extendedWordFound) {
				Token t=parsingAPhrase.getEntitylessTokenized().getTokens().get(index);
				addResult(parsingAPhrase, index, new QualifiedToken(t, WordType.ProductElement));
			}
		}

	}

	private boolean wereAnyTokensReplacedDueToDependencyTree(AbstractParsingObject parsingAPhrase,
			String expandedWordFromApi) {
		List<ConnectionEntry> connotations = parsingAPhrase.getFromEntityLessConotations();

		TokenizationResults extendedFromApiTokenized = this.tokenizator.parse(expandedWordFromApi);
		List<ConnectionEntry> dependenciesFromExtendedWord = extendedFromApiTokenized.getAllTwoWordDependencies();

		for(ConnectionEntry connotationFromExendedPhrase:dependenciesFromExtendedWord) {
			for(ConnectionEntry connotationFromPhrase:connotations) {
				if(areThoseConnectionsBetweenTheSameWords(connotationFromExendedPhrase, connotationFromPhrase)) {
					System.out.println("found");

					goThroughTokensAnMarkConnected(parsingAPhrase, connotationFromExendedPhrase);

					return true;
				}

			}
		}
		return false;
	}

	private void goThroughTokensAnMarkConnected(AbstractParsingObject parsingAPhrase,
			ConnectionEntry connotationFromExendedPhrase) {
		boolean headFound=false,childFound=false;
		//check current token
		if(parsingAPhrase.getFinalResults().size()<parsingAPhrase.getEntitylessTokenized().getTokens().size()&&(!headFound||!childFound)) {
			Token currentToken = parsingAPhrase.getEntitylessTokenized().getTokens().get(parsingAPhrase.getFinalResults().size());
			if(!headFound) {
				if(currentToken.getText().equals(connotationFromExendedPhrase.getHead().getText())
						||currentToken.getLemma().equals(connotationFromExendedPhrase.getHead().getLemma())) {
					parsingAPhrase.getFinalResults().add(new QualifiedToken(connotationFromExendedPhrase.getHead(),WordType.ProductElement));
					parsingAPhrase.getDependencyConotationsFound().add(connotationFromExendedPhrase);
					
					headFound=true;
				}
			}
			if(!childFound) {
				if(currentToken.getText().equals(connotationFromExendedPhrase.getChild().getText())
						||currentToken.getLemma().equals(connotationFromExendedPhrase.getChild().getLemma())) {
					parsingAPhrase.getFinalResults().add(new QualifiedToken(connotationFromExendedPhrase.getChild(),WordType.ProductElement));
					childFound=true;
				}
			}
		}
		//checking past tokens
		for(int i=0;i<parsingAPhrase.getFinalResults().size()&&(!headFound||!childFound);i++) {
			if((connotationFromExendedPhrase.getHead().getText().equals(parsingAPhrase.getFinalResults().get(i).getText()))
					||(connotationFromExendedPhrase.getHead().getLemma().equals(parsingAPhrase.getFinalResults().get(i).getLemma()))) {
				if(parsingAPhrase.getFinalResults().get(i).getWordType()!=null) {
					System.out.println("already classified word classified again due to dependency: "+i);
				}else {

					QualifiedToken result=new QualifiedToken(connotationFromExendedPhrase.getHead().getText(),
							parsingAPhrase.getFinalResults().get(i).getLemma(), parsingAPhrase.getFinalResults().get(i).getTag(), WordType.ProductElement);
					parsingAPhrase.getFinalResults().set(i, result);
					parsingAPhrase.getDependencyConotationsFound().add(connotationFromExendedPhrase);

					headFound=true;
				}
			}else if((connotationFromExendedPhrase.getChild().getText().equals(parsingAPhrase.getFinalResults().get(i).getText()))
					||(connotationFromExendedPhrase.getChild().getLemma().equals(parsingAPhrase.getFinalResults().get(i).getLemma()))) {
				if(parsingAPhrase.getFinalResults().get(i).getWordType()!=null) {
					System.out.println("already classified word classified again due to dependency: "+i);
				}else {

					QualifiedToken result=new QualifiedToken(connotationFromExendedPhrase.getChild().getText(),
							parsingAPhrase.getFinalResults().get(i).getLemma(), parsingAPhrase.getFinalResults().get(i).getTag(), WordType.ProductElement);
					parsingAPhrase.getFinalResults().set(i, result);
				}
				childFound=true;
			}

		}
		//if still not both found, check future tokens as well
		if(parsingAPhrase.getEntitylessTokenized().getTokens().size()>parsingAPhrase.getFinalResults().size()&&(!headFound||!childFound)) {

			Token currentToken = parsingAPhrase.getEntitylessTokenized().getTokens().get(parsingAPhrase.getFinalResults().size());
			if(!headFound) {
				if(currentToken.getText().equals(connotationFromExendedPhrase.getHead().getText())
						||currentToken.getLemma().equals(connotationFromExendedPhrase.getHead().getLemma())) {
					QualifiedToken result=new QualifiedToken(connotationFromExendedPhrase.getHead().getText(),
							connotationFromExendedPhrase.getHead().getLemma(), connotationFromExendedPhrase.getHead().getTag(), WordType.ProductElement);
					parsingAPhrase.getFinalResults().add(result);
					parsingAPhrase.getDependencyConotationsFound().add(connotationFromExendedPhrase);

					headFound=true;
				}
			}
			if(!childFound) {
				if(currentToken.getText().equals(connotationFromExendedPhrase.getChild().getText())
						||currentToken.getLemma().equals(connotationFromExendedPhrase.getChild().getLemma())) {
					QualifiedToken result=new QualifiedToken(connotationFromExendedPhrase.getChild().getText(),
							connotationFromExendedPhrase.getChild().getLemma(), connotationFromExendedPhrase.getChild().getTag(), WordType.ProductElement);
					parsingAPhrase.getFinalResults().add(result);
					childFound=true;
				}
			}
		}

	}



	private boolean areThoseConnectionsBetweenTheSameWords(ConnectionEntry connotationFromExendedPhrase,
			ConnectionEntry connotationFromPhrase) {
		if(connotationFromPhrase.getHead().getText().equals(connotationFromExendedPhrase.getHead().getText())
				||connotationFromPhrase.getHead().getLemma().equals(connotationFromExendedPhrase.getHead().getLemma())){
			if(connotationFromPhrase.getChild().getText().equals(connotationFromExendedPhrase.getChild().getText())
					||connotationFromPhrase.getChild().getLemma().equals(connotationFromExendedPhrase.getChild().getLemma())){
				return true;
			}
		}
		return false;


	}

	private boolean wereAnyTokensMarkedBesideCurrentDueToAdjacency(AbstractParsingObject parsingAPhrase, int index,
			String expandedWordFromApi, int expandedWordFromApiLength,
			List<Token> actualTokens) {
		if(parsingAPhrase.getEntitylessString().indexOf(expandedWordFromApi)>=0){

			if(expandedWordFromApiLength>1) {
				if(index-expandedWordFromApiLength>=0) {
					//start at first wor
					wereAnyTokensMarkedBeforeCurrentOne(parsingAPhrase, index, expandedWordFromApi,
							expandedWordFromApiLength, actualTokens);
					return true;

					//does it end after current index?
				}else if(expandedWordFromApiLength+index<=actualTokens.size()) {
					if(wereAnyTokensMarkedAfterCurrent(parsingAPhrase, index, expandedWordFromApi,
							expandedWordFromApiLength, actualTokens)) {
						return true;
					}
				}else {
					System.err.println("well, we got some word in the middle of sentence case in word api");
				}
			}
		}
		return false;
	}

	private boolean wereAnyTokensMarkedBeforeCurrentOne(AbstractParsingObject parsingAPhrase, int index,
			String expandedWordFromApi, int expandedWordFromApiLength, List<Token> actualTokens) {
		List<Token> subList=actualTokens.subList(index-expandedWordFromApiLength+1, index+1);



		String fused=subList.stream().map(s->s.getText()).collect( Collectors.joining(" ") );
		if(fused.indexOf(expandedWordFromApi)>=0) {
			List<String> conotation=new ArrayList<String>();
			//		QualifiedToken result=new QualifiedToken(fused, "fused", "fused", WordType.ProductElement);
			for(int i=index-expandedWordFromApiLength+1;i<=index;i++) {
				QualifiedToken resultQt=new QualifiedToken(actualTokens.get(i),WordType.ProductElement);
				resultQt.setMergeType(MergeType.ADJACENCY);
				parsingAPhrase.getFinalResults().set(i,resultQt );
				addResult(parsingAPhrase, index, resultQt);
				conotation.add(actualTokens.get(i).getText());
			}
			parsingAPhrase.getAdjacentyConotationsFound().add(conotation) ;

			return true;
		}else {
			return false;
		}
	}

	private boolean wereAnyTokensMarkedAfterCurrent(AbstractParsingObject parsingAPhrase, int index,
			String expandedWordFromApi, int expandedWordFromApiLength,
			List<Token> actualTokens) {
		List<Token> subList = actualTokens.subList(index, expandedWordFromApiLength+index);
		String fused=subList.stream().map(s->s.getText()).collect( Collectors.joining(" ") );
		if(fused.indexOf(expandedWordFromApi)>=0) {
			//			QualifiedToken result=QualifiedToken.createMerged(fused,WordType.ProductElement);
			//
			//			addOtherWordsFromExpandedTofututreTokens(index, parsingAPhrase,expandedWordFromApi);
			for(int i=index;i<expandedWordFromApiLength+index;i++) {
				QualifiedToken resultQt=new QualifiedToken(subList.get(i),WordType.ProductElement);
				resultQt.setMergeType(MergeType.ADJACENCY);


				addResult(parsingAPhrase, i, resultQt);
				parsingAPhrase.addFutureToken(i, resultQt);

			}
			return true;
		}else {
			return false;
		}

	}

	private void addOtherWordsFromExpandedTofututreTokens(int index, AbstractParsingObject parsingAPhrase,
			String expandedWordFromApi) {

		String[] split = expandedWordFromApi.split(" ");
		for(int i=index;i<split.length+index;i++) {

			parsingAPhrase.addFutureToken(i, new QualifiedToken(parsingAPhrase.getEntitylessTokenized().getTokens().get(i), WordType.ProductElement));
		}
	}

	private static WordsApiResult checkProductTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
		for(WordsApiResult war:wordResults) {
			if(checkIfPropertiesFromWordsApiContainKeywords(war.getOriginalWord(),war.getTypeOf(),productTypeKeywords)) {
				return war;
			}
		}
		return null;		
	}


	private static WordsApiResult checkQuantityTypesForWordObject(ArrayList<WordsApiResult> wordResults) {
		for(WordsApiResult war:wordResults) {
			if(war instanceof WordsApiResultImpostor
					||checkIfPropertiesFromWordsApiContainKeywords(war.getOriginalWord(),war.getTypeOf(),quantityTypeKeywords)
					||checkIfPropertiesFromWordsApiContainKeywords(war.getOriginalWord(),war.getAttribute(),quantityAttributeKeywords)) {
				return war;
			}
		}
		return null;		
	}




	private static boolean checkIfPropertiesFromWordsApiContainKeywords(String productName, ArrayList<String> typeResults,ArrayList<String> keywords) {
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

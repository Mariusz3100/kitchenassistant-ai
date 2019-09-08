package mariusz.ambroziak.kassistant.ai.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;



@Service
public class IngredientPhraseParser {
	private TokenizationClientService tokenizator;
	private NamedEntityRecognitionClientService nerRecognizer;
	private ResourceLoader resourceLoader;
	private Resource inputFileResource;
	
	
	

	public IngredientPhraseParser(TokenizationClientService tokenizator,
			NamedEntityRecognitionClientService nerRecognizer, ResourceLoader resourceLoader) {
		super();
		this.tokenizator = tokenizator;
		this.nerRecognizer = nerRecognizer;
		this.resourceLoader = resourceLoader;
		this.inputFileResource=this.resourceLoader.getResource("classpath:/teachingResources/wordsInput");
	}




	public ModelAndView parseFromFile() throws IOException {
		String retValue="";
		InputStream inputStream = inputFileResource.getInputStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
		
		String line=br.readLine();
		ArrayList<String> phrases=new ArrayList<String>();

		while(line!=null) {
//			NerResults parsed = this.nerRecognizer.find(line);
//			
//			retValue+=parsed.getPhrase()+" : "+parsed.getSpansAsString();
//			
			phrases.add(line);
			
			line=br.readLine();
		}
		ModelAndView mav=new ModelAndView("ingredientCategoriesList");
		mav.addObject("phrases", phrases);
		return mav;
		
	}
	
	
	
}

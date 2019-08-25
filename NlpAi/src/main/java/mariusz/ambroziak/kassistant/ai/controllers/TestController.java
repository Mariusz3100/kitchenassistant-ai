package mariusz.ambroziak.kassistant.ai.controllers;

import org.springframework.web.bind.annotation.RestController;

import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NamedEntityRecognitionClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationClientService;
import mariusz.ambroziak.kassistant.ai.nlpclients.tokenization.TokenizationResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class TestController {
    
	TokenizationClientService tokenizationService;
	NamedEntityRecognitionClientService nerService;
	
	


	@Autowired
	public TestController(TokenizationClientService tokenizationService,
			NamedEntityRecognitionClientService nerService) {
		super();
		this.tokenizationService = tokenizationService;
		this.nerService = nerService;
	}
	@RequestMapping("/springTokenize")
    public String springTokenize(@RequestParam(value="param", defaultValue="empty") String param){
    	TokenizationResults retValue=this.tokenizationService.parse(param);
    	return retValue.toString();
    	
    }
	@RequestMapping("/springNer")
    public String springNer(@RequestParam(value="param", defaultValue="empty") String param){
    	NerResults retValue=this.nerService.find(param);
    	return retValue.toString();
    	
		
		
    }
    
}

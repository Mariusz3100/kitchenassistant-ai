package mariusz.ambroziak.kassistant.ai.nlpclients.tokenization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenizationClientService {

	private final RestTemplate restTemplate;

	
	@Autowired
	public TokenizationClientService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public TokenizationResults parse(String name) {
		TokenizationResults retValue=this.restTemplate.getForObject("http://127.0.0.1:8000/tokenizer?param={param}", TokenizationResults.class,name);
		
		return retValue;
	}

	
}

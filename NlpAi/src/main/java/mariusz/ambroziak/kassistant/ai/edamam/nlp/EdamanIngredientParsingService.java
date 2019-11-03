package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EdamanIngredientParsingService {

	private final RestTemplate restTemplate;
	
	
	private final String baseUrl="https://api.edamam.com/api/nutrition-details?app_id=1d006ca9&app_key=d089c348b9338fc421bdc6695ff34e8c";
	
	
	
	
	@Autowired
	public EdamanIngredientParsingService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public EdamamNlpResponseData find(List<String> ingredientLines) {
		if(ingredientLines.isEmpty()) {
			return EdamamNlpResponseData.createEmpty();
		}
		JSONObject bodyJson=new JSONObject();
		JSONArray jArr=new JSONArray();
		for(String ingLine:ingredientLines) {
			jArr.put(ingLine);
		}
		
		bodyJson.put("ingr", jArr);

		final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        final HttpEntity<String> entity = new HttpEntity<String>(bodyJson.toString(),headers);
        
		ResponseEntity<EdamamNlpResponseData> retValue=this.restTemplate.exchange(baseUrl, HttpMethod.POST,entity, EdamamNlpResponseData.class);
		
		return retValue.getBody();
	}

	public EdamamNlpResponseData find(String param) {
		if(param==null||param.isEmpty()) {
			return EdamamNlpResponseData.createEmpty();
		}
		List<String> paramList=new ArrayList<String>();
		paramList.add(param);
		return find(paramList);
	}

	
}

package mariusz.ambroziak.kassistant.ai.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import mariusz.ambroziak.kassistant.ai.logic.IngredientPhraseParser;
import mariusz.ambroziak.kassistant.ai.nlpclients.ner.NerResults;

@RestController
public class LogicController {

	private IngredientPhraseParser ingredientParser;


	@Autowired
	public LogicController(IngredientPhraseParser ingredientParser) {
		super();
		this.ingredientParser = ingredientParser;
	}



	@RequestMapping("/parseIngredients")
	public ModelAndView springNer() throws IOException{
		ModelAndView parseFromFile = this.ingredientParser.parseFromFile();

		System.out.println();

		return parseFromFile;

	}
}

package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import mariusz.ambroziak.kassistant.ai.enums.AmountTypes;

public class LearningTuple extends EdamamNlpSingleIngredientInner{
	private String originalPhrase;
	private float amount;
	private AmountTypes amountType;
	
	
	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public AmountTypes getAmountType() {
		return amountType;
	}

	public void setAmountType(AmountTypes amountType) {
		this.amountType = amountType;
	}

//	public ExpectedResult(float quantity, String measure, String foodMatch, AmountTypes amountType) {
//		super(quantity, measure, foodMatch);
//		PreciseQuantity resultingQuantity = EdamanApiQuantityExtractor.getResultingQuantity(quantity, measure);
//		
//		this.amount = quantity*resultingQuantity.getAmount();
//		this.amountType = resultingQuantity.getType();
//	}

	public LearningTuple(String originalPhrase,float quantity, String measure, String foodMatch,  AmountTypes amountType) {
		super(quantity, measure, foodMatch);
		PreciseQuantity resultingQuantity = EdamanApiQuantityExtractor.getResultingQuantity(quantity, measure);

		this.originalPhrase = originalPhrase;
		this.amount = resultingQuantity.getAmount();
		this.amountType = resultingQuantity.getType();
		
	}

	public String getOriginalPhrase() {
		return originalPhrase;
	}

	public void setOriginalPhrase(String originalPhrase) {
		this.originalPhrase = originalPhrase;
	}

	
	
	
	
	
}

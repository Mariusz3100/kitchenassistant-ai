package mariusz.ambroziak.kassistant.ai.edamam.nlp;

import mariusz.ambroziak.kassistant.ai.enums.AmountTypes;

public class ExpectedResult extends EdamamNlpSingleIngredientInner{
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

	public ExpectedResult(float quantity, String measure, String foodMatch, AmountTypes amountType) {
		super(quantity, measure, foodMatch);
		PreciseQuantity resultingQuantity = EdamanApiQuantityExtractor.getResultingQuantity(quantity, measure);
		
		this.amount = quantity*resultingQuantity.getAmount();
		this.amountType = resultingQuantity.getType();
	}

	
	
	
	
	
}

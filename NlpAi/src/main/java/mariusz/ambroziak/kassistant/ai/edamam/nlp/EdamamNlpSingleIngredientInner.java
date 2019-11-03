package mariusz.ambroziak.kassistant.ai.edamam.nlp;

public class EdamamNlpSingleIngredientInner {
	private int quantity;
	private String measure;
	private String foodMatch;

	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public String getFoodMatch() {
		return foodMatch;
	}
	public void setFoodMatch(String foodMatch) {
		this.foodMatch = foodMatch;
	}
}

package mariusz.ambroziak.kassistant.ai.tesco;

public class Tesco_Product extends ProductData{


	private String quantityString;
	private String superdepartment;

	public String getQuantityString() {
		return quantityString;
	}
	public void setQuantityString(String quantityString) {
		this.quantityString = quantityString;
	}

	public String getSuperdepartment() {
		return superdepartment;
	}
	public void setSuperdepartment(String superdepartment) {
		this.superdepartment = superdepartment;
	}
	public Tesco_Product(String name, String detailsUrl, String description, String quantityString, String department,
			String superdepartment) {
		super();
		this.name = name;
		this.detailsUrl = detailsUrl;
		this.description = description;
		this.quantityString = quantityString;
		this.department = department;
		this.superdepartment = superdepartment;
	}
	

}

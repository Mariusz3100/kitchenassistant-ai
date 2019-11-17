package mariusz.ambroziak.kassistant.ai.tesco;

public class Tesco_Product {


	private String name;
	private String detailsUrl;
	private String description;
	private String quantityString;
	private String department;
	private String superdepartment;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDetailsUrl() {
		return detailsUrl;
	}
	public void setDetailsUrl(String detailsUrl) {
		this.detailsUrl = detailsUrl;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getQuantityString() {
		return quantityString;
	}
	public void setQuantityString(String quantityString) {
		this.quantityString = quantityString;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
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

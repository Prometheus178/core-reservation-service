package org.booking.core.integration.request;


public class BusinessRequest {
	private String type;
	private String name;
	private String address;
	private String description;
	private BusinessHoursRequest businessHours;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BusinessHoursRequest getBusinessHours() {
		return businessHours;
	}

	public void setBusinessHours(BusinessHoursRequest businessHours) {
		this.businessHours = businessHours;
	}
}

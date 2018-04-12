package io.tripmate.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * MTN payment charging information data model
 */
public class ChargeInformation {
	@SerializedName("amount")
	private long amount;
	@SerializedName("code")
	private String code;
	@SerializedName("currency")
	private String currency;
	@SerializedName("description")
	private ArrayList<String> description;
	
	//Default constructor for serialization
	public ChargeInformation() {
	}
	
	public ChargeInformation(long amount, String code, String currency, ArrayList<String> description) {
		this.amount = amount;
		this.code = code;
		this.currency = currency;
		this.description = description;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public ArrayList<String> getDescription() {
		return description;
	}
}

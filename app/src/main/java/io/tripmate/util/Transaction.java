package io.tripmate.util;

import com.google.gson.annotations.SerializedName;

/**
 * MTN transaction data model
 */
public class Transaction {
	@SerializedName("endUserId")
	private long userId;
	@SerializedName("paymentAmount")
	private ChargeInformation chargeInformation;
	@SerializedName("referenceCode")
	private String code;
	@SerializedName("transactionStatus")
	private String status;
	
	//Default constructor for serialization
	public Transaction() {
	}
	
	public Transaction(long userId, ChargeInformation chargeInformation, String code, String status) {
		this.userId = userId;
		this.chargeInformation = chargeInformation;
		this.code = code;
		this.status = status;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public ChargeInformation getChargeInformation() {
		return chargeInformation;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getStatus() {
		return status;
	}
}

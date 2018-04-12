package io.tripmate.util;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Mobile Money payment API
 */
public interface PaymentAPI {
	String BASE_URL = "http://10.137.213.125:14312/1/";
	
	//Make payment with MTN Mobile money
	@POST("/payment/{endUserId}/transactions/amount")
	Call<Transaction> makePayment(@Path("endUserId") String phoneNumber);
}

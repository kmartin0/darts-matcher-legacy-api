package com.dartsmatcher.legacy.features.x01.x01checkout;

import com.dartsmatcher.legacy.features.x01.x01match.models.checkout.X01Checkout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public interface IX01CheckoutService {

	ArrayList<X01Checkout> getCheckouts() throws IOException;

	Optional<X01Checkout> getCheckout(int remaining) throws IOException;

}

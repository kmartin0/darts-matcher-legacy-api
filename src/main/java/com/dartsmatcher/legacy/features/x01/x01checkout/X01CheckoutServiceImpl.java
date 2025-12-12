package com.dartsmatcher.legacy.features.x01.x01checkout;

import com.dartsmatcher.legacy.features.x01.x01match.models.checkout.X01Checkout;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class X01CheckoutServiceImpl implements IX01CheckoutService {

    @Value("classpath:data/checkouts.json")
    private Resource checkoutsResourceFile;


    @Override
    public ArrayList<X01Checkout> getCheckouts() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(checkoutsResourceFile.getInputStream(), new TypeReference<ArrayList<X01Checkout>>() {
        });

    }

    @Override
    public Optional<X01Checkout> getCheckout(int remaining) throws IOException {
        return getCheckouts().stream()
                .filter(x01Checkout -> x01Checkout.getCheckout() == remaining)
                .findFirst();
    }
}

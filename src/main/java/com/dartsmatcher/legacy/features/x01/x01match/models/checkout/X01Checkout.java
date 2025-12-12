package com.dartsmatcher.legacy.features.x01.x01match.models.checkout;

import com.dartsmatcher.legacy.features.dartboard.Dart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Checkout {

    public X01Checkout(X01Checkout x01Checkout) {
        this(
                x01Checkout.getCheckout(),
                x01Checkout.getMinDarts(),
                x01Checkout.getSuggested() == null ? new ArrayList<>() : x01Checkout.getSuggested().stream().map(Dart::new).collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private int checkout;

    private int minDarts;

    private ArrayList<Dart> suggested;

}

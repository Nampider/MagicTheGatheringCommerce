package com.tradingcard.commerce.dto;

public record CreateCheckoutRequest(
        String productName,
        Long amountCents,
        String currency
) {}

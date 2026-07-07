package com.tradingcard.commerce.dto;

import java.util.UUID;

public record CreateCheckoutResponse(
        UUID paymentId,
        String checkoutSessionId,
        String checkoutUrl
) {
}

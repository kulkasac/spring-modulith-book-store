package com.bookstore.payments;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(
        displayName = "payments",
        allowedDependencies = {"orders::events"}
)
public class PaymentConfiguration {
}

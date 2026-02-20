package com.bookstore.inventory;

import org.springframework.modulith.ApplicationModule;

@ApplicationModule(
    displayName = "Inventory",
    allowedDependencies = {"orders::events"}
)
public class InventoryConfiguration {
}

// src/main/java/com/bookstore/inventory/package-info.java
/**
 * The Inventory module manages the book catalog and stock levels.
 * It reacts to {@link com.bookstore.orders.events.OrderPlacedEvent} to
 * atomically decrease stock within the same transaction boundary.
 *
 * <p>Allowed dependencies: {@code orders::events}</p>
 */
package com.bookstore.inventory;
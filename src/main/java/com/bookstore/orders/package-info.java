/**
 * The Orders module is responsible for capturing customer purchase intent.
 * It creates {@link com.bookstore.orders.domain.Order} aggregates and publishes
 * {@link com.bookstore.orders.events.OrderPlacedEvent} for downstream modules to react.
 *
 * <p>Exposed interface: {@code orders::events}</p>
 * <p>Allowed dependencies: none</p>
 */
package com.bookstore.orders;
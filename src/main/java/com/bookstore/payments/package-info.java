/**
 * The Payments module handles payment processing for placed orders.
 * It reacts to {@code OrderPlacedEvent} and publishes either
 * {@code PaymentCompletedEvent} or {@code PaymentFailedEvent}.
 *
 * <p>Exposed named interface: {@code payment::events}</p>
 * <p>Allowed dependencies: {@code orders::events}</p>
 */
package com.bookstore.payments;
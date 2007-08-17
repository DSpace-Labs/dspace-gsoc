package org.dspace.statistics.web;

import java.util.*;

public class OrderDetail {

	int id;
	String customerName;
	String country;
	int orderNumber;
	Date orderDate;
	Date shippedDate;
	String productName;
	int quantity;
	float total;

	public final String getCountry() {
		return country;
	}

	public final void setCountry(String country) {
		this.country = country;
	}

	public final String getCustomerName() {
		return customerName;
	}

	public final void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final Date getOrderDate() {
		return orderDate;
	}

	public final void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public final int getOrderNumber() {
		return orderNumber;
	}

	public final void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public final String getProductName() {
		return productName;
	}

	public final void setProductName(String productName) {
		this.productName = productName;
	}

	public final int getQuantity() {
		return quantity;
	}

	public final void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public final Date getShippedDate() {
		return shippedDate;
	}

	public final void setShippedDate(Date shippedDate) {
		this.shippedDate = shippedDate;
	}

	public final float getTotal() {
		return total;
	}

	public final void setTotal(float total) {
		this.total = total;
	}
}


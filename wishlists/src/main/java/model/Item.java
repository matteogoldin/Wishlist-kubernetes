package model;

import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class Item {
	private String name;
	private String description;
	private float price;

	public Item() {}

	public Item(String name, String desc, float price) {
		this.name = name;
		this.description = desc;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return description;
	}

	public void setDesc(String desc) {
		this.description = desc;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, name, price);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		Item other = (Item) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name)
				&& Float.floatToIntBits(price) == Float.floatToIntBits(other.price);
	}

	@Override
	public String toString() {
		return this.name;
	}
}

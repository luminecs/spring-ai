package org.springframework.ai.image;

import java.util.Objects;

public class ImageMessage {

	private String text;

	private Float weight;

	public ImageMessage(String text) {
		this.text = text;
	}

	public ImageMessage(String text, Float weight) {
		this.text = text;
		this.weight = weight;
	}

	public String getText() {
		return this.text;
	}

	public Float getWeight() {
		return this.weight;
	}

	@Override
	public String toString() {
		return "ImageMessage{" + "text='" + this.text + '\'' + ", weight=" + this.weight + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ImageMessage that)) {
			return false;
		}
		return Objects.equals(this.text, that.text) && Objects.equals(this.weight, that.weight);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.text, this.weight);
	}

}

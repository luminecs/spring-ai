package org.springframework.ai.image;

import java.util.Objects;

public class Image {

	private String url;

	private String b64Json;

	public Image(String url, String b64Json) {
		this.url = url;
		this.b64Json = b64Json;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getB64Json() {
		return this.b64Json;
	}

	public void setB64Json(String b64Json) {
		this.b64Json = b64Json;
	}

	@Override
	public String toString() {
		return "Image{" + "url='" + this.url + '\'' + ", b64Json='" + this.b64Json + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Image image)) {
			return false;
		}
		return Objects.equals(this.url, image.url) && Objects.equals(this.b64Json, image.b64Json);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.url, this.b64Json);
	}

}

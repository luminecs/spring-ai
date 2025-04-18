package org.springframework.ai.content;

import java.io.IOException;
import java.net.URL;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

public class Media {

	private static final String NAME_PREFIX = "media-";

	@Nullable
	private String id;

	private final MimeType mimeType;

	private final Object data;

	private String name;

	public Media(MimeType mimeType, URL url) {
		Assert.notNull(mimeType, "MimeType must not be null");
		Assert.notNull(url, "URL must not be null");
		this.mimeType = mimeType;
		this.id = null;
		this.data = url.toString();
		this.name = generateDefaultName(mimeType);
	}

	public Media(MimeType mimeType, Resource resource) {
		Assert.notNull(mimeType, "MimeType must not be null");
		Assert.notNull(resource, "Data must not be null");
		try {
			byte[] bytes = resource.getContentAsByteArray();
			this.mimeType = mimeType;
			this.id = null;
			this.data = bytes;
			this.name = generateDefaultName(mimeType);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final Builder builder() {
		return new Builder();
	}

	private Media(MimeType mimeType, Object data, String id, String name) {
		Assert.notNull(mimeType, "MimeType must not be null");
		Assert.notNull(data, "Data must not be null");
		this.mimeType = mimeType;
		this.id = id;
		this.name = (name != null) ? name : generateDefaultName(mimeType);
		this.data = data;
	}

	private static String generateDefaultName(MimeType mimeType) {
		return NAME_PREFIX + mimeType.getSubtype() + "-" + java.util.UUID.randomUUID();
	}

	public MimeType getMimeType() {
		return this.mimeType;
	}

	public Object getData() {
		return this.data;
	}

	public byte[] getDataAsByteArray() {
		if (this.data instanceof byte[]) {
			return (byte[]) this.data;
		}
		else {
			throw new IllegalStateException("Media data is not a byte[]");
		}
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public static final class Builder {

		private String id;

		private MimeType mimeType;

		private Object data;

		private String name;

		private Builder() {
		}

		public Builder mimeType(MimeType mimeType) {
			Assert.notNull(mimeType, "MimeType must not be null");
			this.mimeType = mimeType;
			return this;
		}

		public Builder data(Resource resource) {
			Assert.notNull(resource, "Data must not be null");
			try {
				this.data = resource.getContentAsByteArray();
			}
			catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
			return this;
		}

		public Builder data(Object data) {
			Assert.notNull(data, "Data must not be null");
			this.data = data;
			return this;
		}

		public Builder data(URL url) {
			Assert.notNull(url, "URL must not be null");
			this.data = url.toString();
			return this;
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Media build() {
			return new Media(this.mimeType, this.data, this.id, this.name);
		}

	}

	public static class Format {

		public static final MimeType DOC_PDF = MimeType.valueOf("application/pdf");

		public static final MimeType DOC_CSV = MimeType.valueOf("text/csv");

		public static final MimeType DOC_DOC = MimeType.valueOf("application/msword");

		public static final MimeType DOC_DOCX = MimeType
			.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

		public static final MimeType DOC_XLS = MimeType.valueOf("application/vnd.ms-excel");

		public static final MimeType DOC_XLSX = MimeType
			.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		public static final MimeType DOC_HTML = MimeType.valueOf("text/html");

		public static final MimeType DOC_TXT = MimeType.valueOf("text/plain");

		public static final MimeType DOC_MD = MimeType.valueOf("text/markdown");

		public static final MimeType VIDEO_MKV = MimeType.valueOf("video/x-matros");

		public static final MimeType VIDEO_MOV = MimeType.valueOf("video/quicktime");

		public static final MimeType VIDEO_MP4 = MimeType.valueOf("video/mp4");

		public static final MimeType VIDEO_WEBM = MimeType.valueOf("video/webm");

		public static final MimeType VIDEO_FLV = MimeType.valueOf("video/x-flv");

		public static final MimeType VIDEO_MPEG = MimeType.valueOf("video/mpeg");

		public static final MimeType VIDEO_MPG = MimeType.valueOf("video/mpeg");

		public static final MimeType VIDEO_WMV = MimeType.valueOf("video/x-ms-wmv");

		public static final MimeType VIDEO_THREE_GP = MimeType.valueOf("video/3gpp");

		public static final MimeType IMAGE_PNG = MimeType.valueOf("image/png");

		public static final MimeType IMAGE_JPEG = MimeType.valueOf("image/jpeg");

		public static final MimeType IMAGE_GIF = MimeType.valueOf("image/gif");

		public static final MimeType IMAGE_WEBP = MimeType.valueOf("image/webp");

	}

}

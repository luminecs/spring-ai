package org.springframework.ai.embedding;

import org.springframework.ai.model.ResultMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public class EmbeddingResultMetadata implements ResultMetadata {

	public static EmbeddingResultMetadata EMPTY = new EmbeddingResultMetadata();

	private final ModalityType modalityType;

	private final String documentId;

	private final MimeType mimeType;

	private final Object documentData;

	public EmbeddingResultMetadata() {
		this("", ModalityType.TEXT, MimeTypeUtils.TEXT_PLAIN, null);
	}

	public EmbeddingResultMetadata(String documentId, ModalityType modalityType, MimeType mimeType,
			Object documentData) {
		Assert.notNull(modalityType, "ModalityType must not be null");
		Assert.notNull(mimeType, "MimeType must not be null");

		this.documentId = documentId;
		this.modalityType = modalityType;
		this.mimeType = mimeType;
		this.documentData = documentData;
	}

	public ModalityType getModalityType() {
		return this.modalityType;
	}

	public MimeType getMimeType() {
		return this.mimeType;
	}

	public String getDocumentId() {
		return this.documentId;
	}

	public Object getDocumentData() {
		return this.documentData;
	}

	public enum ModalityType {

		TEXT, IMAGE, AUDIO, VIDEO

	}

	public static class ModalityUtils {

		private static final MimeType TEXT_MIME_TYPE = MimeTypeUtils.parseMimeType("text/*");

		private static final MimeType IMAGE_MIME_TYPE = MimeTypeUtils.parseMimeType("image/*");

		private static final MimeType VIDEO_MIME_TYPE = MimeTypeUtils.parseMimeType("video/*");

		private static final MimeType AUDIO_MIME_TYPE = MimeTypeUtils.parseMimeType("audio/*");

		public static ModalityType getModalityType(MimeType mimeType) {

			if (mimeType == null) {
				return ModalityType.TEXT;
			}

			if (mimeType.isCompatibleWith(IMAGE_MIME_TYPE)) {
				return ModalityType.IMAGE;
			}
			else if (mimeType.isCompatibleWith(AUDIO_MIME_TYPE)) {
				return ModalityType.AUDIO;
			}
			else if (mimeType.isCompatibleWith(VIDEO_MIME_TYPE)) {
				return ModalityType.VIDEO;
			}
			else if (mimeType.isCompatibleWith(TEXT_MIME_TYPE)) {
				return ModalityType.TEXT;
			}

			throw new IllegalArgumentException("Unsupported MimeType: " + mimeType);
		}

	}

}

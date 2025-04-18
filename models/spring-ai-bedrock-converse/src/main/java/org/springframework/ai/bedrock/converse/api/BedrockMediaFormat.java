package org.springframework.ai.bedrock.converse.api;

import java.util.Map;

import software.amazon.awssdk.services.bedrockruntime.model.DocumentFormat;
import software.amazon.awssdk.services.bedrockruntime.model.ImageFormat;
import software.amazon.awssdk.services.bedrockruntime.model.VideoFormat;

import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;

public abstract class BedrockMediaFormat {

	// @formatter:off
	public static final Map<MimeType, DocumentFormat> DOCUMENT_MAP = Map.of(
		Media.Format.DOC_PDF, DocumentFormat.PDF,
		Media.Format.DOC_CSV, DocumentFormat.CSV,
		Media.Format.DOC_DOC, DocumentFormat.DOC,
		Media.Format.DOC_DOCX, DocumentFormat.DOCX,
		Media.Format.DOC_XLS, DocumentFormat.XLS,
		Media.Format.DOC_XLSX, DocumentFormat.XLSX,
		Media.Format.DOC_HTML, DocumentFormat.HTML,
		Media.Format.DOC_TXT, DocumentFormat.TXT,
		Media.Format.DOC_MD, DocumentFormat.MD);
	// @formatter:on

	// @formatter:off
	public static final Map<MimeType, ImageFormat> IMAGE_MAP = Map.of(
		Media.Format.IMAGE_JPEG, ImageFormat.JPEG,
		Media.Format.IMAGE_PNG, ImageFormat.PNG,
		Media.Format.IMAGE_GIF, ImageFormat.GIF,
		Media.Format.IMAGE_WEBP, ImageFormat.WEBP);
	// @formatter:on

	// @formatter:off
	public static final Map<MimeType, VideoFormat> VIDEO_MAP = Map.of(
		Media.Format.VIDEO_MKV, VideoFormat.MKV,
		Media.Format.VIDEO_MOV, VideoFormat.MOV,
		Media.Format.VIDEO_MP4, VideoFormat.MP4,
		Media.Format.VIDEO_WEBM, VideoFormat.WEBM,
		Media.Format.VIDEO_FLV, VideoFormat.FLV,
		Media.Format.VIDEO_MPEG, VideoFormat.MPEG,
		Media.Format.VIDEO_WMV, VideoFormat.WMV,
		Media.Format.VIDEO_THREE_GP, VideoFormat.THREE_GP);
	// @formatter:on

	public static String getFormatAsString(MimeType mimeType) {
		if (isSupportedDocumentFormat(mimeType)) {
			return DOCUMENT_MAP.get(mimeType).toString();
		}
		else if (isSupportedImageFormat(mimeType)) {
			return IMAGE_MAP.get(mimeType).toString();
		}
		else if (isSupportedVideoFormat(mimeType)) {
			return VIDEO_MAP.get(mimeType).toString();
		}
		throw new IllegalArgumentException("Unsupported media format: " + mimeType);
	}

	public static Boolean isSupportedDocumentFormat(MimeType mimeType) {
		return DOCUMENT_MAP.containsKey(mimeType);
	}

	public static DocumentFormat getDocumentFormat(MimeType mimeType) {
		if (!isSupportedDocumentFormat(mimeType)) {
			throw new IllegalArgumentException("Unsupported document format: " + mimeType);
		}
		return DOCUMENT_MAP.get(mimeType);
	}

	public static Boolean isSupportedImageFormat(MimeType mimeType) {
		return IMAGE_MAP.containsKey(mimeType);
	}

	public static ImageFormat getImageFormat(MimeType mimeType) {
		if (!isSupportedImageFormat(mimeType)) {
			throw new IllegalArgumentException("Unsupported image format: " + mimeType);
		}
		return IMAGE_MAP.get(mimeType);
	}

	public static Boolean isSupportedVideoFormat(MimeType mimeType) {
		return VIDEO_MAP.containsKey(mimeType);
	}

	public static VideoFormat getVideoFormat(MimeType mimeType) {
		if (!isSupportedVideoFormat(mimeType)) {
			throw new IllegalArgumentException("Unsupported video format: " + mimeType);
		}
		return VIDEO_MAP.get(mimeType);
	}

}

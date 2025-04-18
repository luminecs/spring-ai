package org.springframework.ai.bedrock.converse.api;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.bedrockruntime.model.DocumentFormat;
import software.amazon.awssdk.services.bedrockruntime.model.ImageFormat;
import software.amazon.awssdk.services.bedrockruntime.model.VideoFormat;

import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedrockMediaFormatTest {

	@Test
	void testSupportedDocumentFormats() {

		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_PDF)).isEqualTo(DocumentFormat.PDF);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_CSV)).isEqualTo(DocumentFormat.CSV);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_DOC)).isEqualTo(DocumentFormat.DOC);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_DOCX)).isEqualTo(DocumentFormat.DOCX);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_XLS)).isEqualTo(DocumentFormat.XLS);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_XLSX)).isEqualTo(DocumentFormat.XLSX);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_HTML)).isEqualTo(DocumentFormat.HTML);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_TXT)).isEqualTo(DocumentFormat.TXT);
		assertThat(BedrockMediaFormat.DOCUMENT_MAP.get(Media.Format.DOC_MD)).isEqualTo(DocumentFormat.MD);
	}

	@Test
	void testSupportedImageFormats() {

		assertThat(BedrockMediaFormat.IMAGE_MAP.get(Media.Format.IMAGE_JPEG)).isEqualTo(ImageFormat.JPEG);
		assertThat(BedrockMediaFormat.IMAGE_MAP.get(Media.Format.IMAGE_PNG)).isEqualTo(ImageFormat.PNG);
		assertThat(BedrockMediaFormat.IMAGE_MAP.get(Media.Format.IMAGE_GIF)).isEqualTo(ImageFormat.GIF);
		assertThat(BedrockMediaFormat.IMAGE_MAP.get(Media.Format.IMAGE_WEBP)).isEqualTo(ImageFormat.WEBP);
	}

	@Test
	void testSupportedVideoFormats() {

		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_MKV)).isEqualTo(VideoFormat.MKV);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_MOV)).isEqualTo(VideoFormat.MOV);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_MP4)).isEqualTo(VideoFormat.MP4);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_WEBM)).isEqualTo(VideoFormat.WEBM);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_FLV)).isEqualTo(VideoFormat.FLV);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_MPEG)).isEqualTo(VideoFormat.MPEG);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_MPG)).isEqualTo(VideoFormat.MPEG);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_WMV)).isEqualTo(VideoFormat.WMV);
		assertThat(BedrockMediaFormat.VIDEO_MAP.get(Media.Format.VIDEO_THREE_GP)).isEqualTo(VideoFormat.THREE_GP);
	}

	@Test
	void testIsSupportedDocumentFormat() {

		assertThat(BedrockMediaFormat.isSupportedDocumentFormat(Media.Format.DOC_PDF)).isTrue();
		assertThat(BedrockMediaFormat.isSupportedDocumentFormat(Media.Format.DOC_CSV)).isTrue();

		assertThat(BedrockMediaFormat.isSupportedDocumentFormat(MimeType.valueOf("application/unknown"))).isFalse();
	}

	@Test
	void testIsSupportedImageFormat() {

		assertThat(BedrockMediaFormat.isSupportedImageFormat(Media.Format.IMAGE_JPEG)).isTrue();
		assertThat(BedrockMediaFormat.isSupportedImageFormat(Media.Format.IMAGE_PNG)).isTrue();

		assertThat(BedrockMediaFormat.isSupportedImageFormat(MimeType.valueOf("image/tiff"))).isFalse();
	}

	@Test
	void testIsSupportedVideoFormat() {

		assertThat(BedrockMediaFormat.isSupportedVideoFormat(Media.Format.VIDEO_MP4)).isTrue();
		assertThat(BedrockMediaFormat.isSupportedVideoFormat(Media.Format.VIDEO_MOV)).isTrue();

		assertThat(BedrockMediaFormat.isSupportedVideoFormat(MimeType.valueOf("video/avi"))).isFalse();
	}

	@Test
	void testGetFormatAsString() {

		assertThat(BedrockMediaFormat.getFormatAsString(Media.Format.DOC_PDF)).isEqualTo(DocumentFormat.PDF.toString());

		assertThat(BedrockMediaFormat.getFormatAsString(Media.Format.IMAGE_JPEG))
			.isEqualTo(ImageFormat.JPEG.toString());

		assertThat(BedrockMediaFormat.getFormatAsString(Media.Format.VIDEO_MP4)).isEqualTo(VideoFormat.MP4.toString());
	}

	@Test
	void testGetFormatAsStringWithUnsupportedFormat() {

		MimeType unsupportedFormat = MimeType.valueOf("application/unknown");

		assertThatThrownBy(() -> BedrockMediaFormat.getFormatAsString(unsupportedFormat))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unsupported media format: " + unsupportedFormat);
	}

	@Test
	void testGetImageFormat() {

		assertThat(BedrockMediaFormat.getImageFormat(Media.Format.IMAGE_JPEG)).isEqualTo(ImageFormat.JPEG);
		assertThat(BedrockMediaFormat.getImageFormat(Media.Format.IMAGE_PNG)).isEqualTo(ImageFormat.PNG);
	}

}

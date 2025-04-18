package org.springframework.ai.document.id;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.util.Assert;

public class JdkSha256HexIdGenerator implements IdGenerator {

	private static final String SHA_256 = "SHA-256";

	private final String byteHexFormat = "%02x";

	private final Charset charset;

	private final MessageDigest messageDigest;

	public JdkSha256HexIdGenerator(final String algorithm, final Charset charset) {
		this.charset = charset;
		try {
			this.messageDigest = MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public JdkSha256HexIdGenerator() {
		this(SHA_256, StandardCharsets.UTF_8);
	}

	@Override
	public String generateId(Object... contents) {
		return this.hash(this.serializeToBytes(contents));
	}

	private String hash(byte[] contentWithMetadata) {
		byte[] hashBytes = getMessageDigest().digest(contentWithMetadata);
		StringBuilder sb = new StringBuilder();
		for (byte b : hashBytes) {
			sb.append(String.format(this.byteHexFormat, b));
		}
		return UUID.nameUUIDFromBytes(sb.toString().getBytes(this.charset)).toString();
	}

	private byte[] serializeToBytes(Object... contents) {
		Assert.notNull(contents, "Contents must not be null");
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			for (Object content : contents) {
				out.writeObject(content);
			}
			return byteOut.toByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to serialize", e);
		}
	}

	MessageDigest getMessageDigest() {
		try {
			return (MessageDigest) this.messageDigest.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("Unsupported clone for MessageDigest.", e);
		}
	}

}

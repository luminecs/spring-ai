package org.springframework.ai.document.id;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class JdkSha256HexIdGeneratorTest {

	private final JdkSha256HexIdGenerator testee = new JdkSha256HexIdGenerator();

	@Test
	void messageDigestReturnsDistinctInstances() {
		final MessageDigest md1 = this.testee.getMessageDigest();
		final MessageDigest md2 = this.testee.getMessageDigest();

		Assertions.assertThat(md1 != md2).isTrue();

		Assertions.assertThat(md1.getAlgorithm()).isEqualTo(md2.getAlgorithm());
		Assertions.assertThat(md1.getDigestLength()).isEqualTo(md2.getDigestLength());
		Assertions.assertThat(md1.getProvider()).isEqualTo(md2.getProvider());
		Assertions.assertThat(md1.toString()).isEqualTo(md2.toString());
	}

	@Test
	void messageDigestReturnsInstancesWithIndependentAndReproducibleDigests() {
		final String updateString1 = "md1_update";
		final String updateString2 = "md2_update";
		final Charset charset = StandardCharsets.UTF_8;

		final byte[] md1BytesFirstTry = this.testee.getMessageDigest().digest(updateString1.getBytes(charset));
		final byte[] md2BytesFirstTry = this.testee.getMessageDigest().digest(updateString2.getBytes(charset));
		final byte[] md1BytesSecondTry = this.testee.getMessageDigest().digest(updateString1.getBytes(charset));
		final byte[] md2BytesSecondTry = this.testee.getMessageDigest().digest(updateString2.getBytes(charset));

		Assertions.assertThat(md1BytesFirstTry).isNotEqualTo(md2BytesFirstTry);

		Assertions.assertThat(md1BytesFirstTry).isEqualTo(md1BytesSecondTry);
		Assertions.assertThat(md2BytesFirstTry).isEqualTo(md2BytesSecondTry);
	}

}

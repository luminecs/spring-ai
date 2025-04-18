package org.springframework.ai.document.id;

import java.util.UUID;

public class RandomIdGenerator implements IdGenerator {

	@Override
	public String generateId(Object... contents) {
		return UUID.randomUUID().toString();
	}

}

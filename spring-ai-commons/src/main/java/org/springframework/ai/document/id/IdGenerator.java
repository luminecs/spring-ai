package org.springframework.ai.document.id;

public interface IdGenerator {

	String generateId(Object... contents);

}

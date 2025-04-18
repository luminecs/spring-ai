package org.springframework.ai.chat.client;

import org.springframework.lang.Nullable;

public record ResponseEntity<R, E>(@Nullable R response, @Nullable E entity) {

	@Nullable
	public R getResponse() {
		return this.response;
	}

	@Nullable
	public E getEntity() {
		return this.entity;
	}

}

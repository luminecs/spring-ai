package org.springframework.ai.model;

import reactor.core.publisher.Flux;

public interface StreamingModel<TReq extends ModelRequest<?>, TResChunk extends ModelResponse<?>> {

	Flux<TResChunk> stream(TReq request);

}

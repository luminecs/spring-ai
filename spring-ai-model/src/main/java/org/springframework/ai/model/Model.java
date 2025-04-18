package org.springframework.ai.model;

public interface Model<TReq extends ModelRequest<?>, TRes extends ModelResponse<?>> {

	TRes call(TReq request);

}

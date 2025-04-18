package org.springframework.ai.vectorstore.hanadb;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class HanaVectorEntity {

	@Id
	@Column(name = "_id")
	protected String _id;

	public HanaVectorEntity() {
	}

	public String get_id() {
		return this._id;
	}

}

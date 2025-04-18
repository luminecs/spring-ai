package org.springframework.ai.vectorstore.hanadb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "CRICKET_WORLD_CUP")
public class CricketWorldCup extends HanaVectorEntity {

	@Column(name = "content")
	private String content;

	public String getContent() {
		return this.content;
	}

}

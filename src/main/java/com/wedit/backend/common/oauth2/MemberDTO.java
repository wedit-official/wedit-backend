package com.wedit.backend.common.oauth2;

import java.io.Serializable;

import com.wedit.backend.api.member.entity.Member;

import lombok.Getter;

@Getter
public class MemberDTO implements Serializable {
	private String name;
	private String email;

	public MemberDTO(Member userEntity) {
		this.name = userEntity.getName();
		this.email = userEntity.getEmail();
	}
}
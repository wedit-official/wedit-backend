package com.wedit.backend.api.member.repository;

import com.wedit.backend.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByOauthId(String oauthId);

    Optional<Member> findByEmailAndDeletedFalse(String email);

    Optional<Member> findByOauthIdAndDeletedFalse(String oauthId);

    boolean existsByEmailAndDeletedFalse(String email);
}

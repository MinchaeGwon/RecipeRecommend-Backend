package com.recipe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;

@Repository
public interface AccountRepository  extends JpaRepository<Account, Long> {
	// 특정 회원번호, 로그인 타입에 해당하는 회원이 있는지 확인
	Optional<Account> findByLoginidAndType(String loginid, String type);
	
	// 특정 회원번호에 해당하는 회원이 있는지 확인
	Optional<Account> findByLoginid(String loginid);
	
	// 닉네임 중복 확인
	Boolean existsByNickname(String nickname);
	
	Boolean existsByLoginidAndType(String loginid, String type);
}

package com.mar.telegram.db.jpa;

import com.mar.telegram.db.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    UserInfo getByUserId(Long userId);

}

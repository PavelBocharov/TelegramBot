package org.mar.telegram.bot.db.jpa;

import org.mar.telegram.bot.db.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    UserInfo getByUserId(Long userId);

}

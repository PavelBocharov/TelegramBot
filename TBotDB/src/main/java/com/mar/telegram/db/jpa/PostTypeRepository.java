package com.mar.telegram.db.jpa;

import com.mar.telegram.db.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTypeRepository extends JpaRepository<PostType, Long> {

}

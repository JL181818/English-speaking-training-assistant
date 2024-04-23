package com.clankalliance.backbeta.repository;

import com.clankalliance.backbeta.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("from User u where u.phone=?1")
    Optional<User> findByPhone(long phone);

    @Query("from User u where u.id=?1")
    Optional<User> findUserById(long id);

    @Query("from User u where u.phone=?1")
    Optional<User> findUserByPhone(long phone);

    @Query("from User u where ?1='' or cast(u.phone as string ) like %?1% or u.nickName like %?1%" )
    Page<User> findUser(String keyWord, PageRequest request);

}

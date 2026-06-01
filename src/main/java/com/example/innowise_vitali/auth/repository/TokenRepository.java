package com.example.innowise_vitali.auth.repository;

import com.example.innowise_vitali.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    @Query("""
        select t from Token t
        where t.user.id = :userId and t.revoked = false
    """)
    List<Token> findAllValidTokensByUser(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Token t where t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
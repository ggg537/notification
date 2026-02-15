package com.a.repository;

import com.a.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByHandle(String handle);
    Optional<UserEntity> findByHandle(String handle);

    @Query("SELECT u FROM UserEntity u WHERE (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.handle) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND u.deletedAt IS NULL")
    Page<UserEntity> searchByNameOrHandle(@Param("keyword") String keyword, Pageable pageable);

    Optional<UserEntity> findByOauthProviderAndOauthProviderId(String provider, String providerId);
}

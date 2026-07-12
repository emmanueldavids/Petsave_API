package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.SitterWallet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SitterWalletRepository extends JpaRepository<SitterWallet, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<SitterWallet> findByUserId(Long userId);
}

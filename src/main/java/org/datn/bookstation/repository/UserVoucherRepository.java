package org.datn.bookstation.repository;

import org.datn.bookstation.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    
    @Query("SELECT uv FROM UserVoucher uv WHERE uv.user.id = :userId AND uv.voucher.id = :voucherId")
    Optional<UserVoucher> findByUserIdAndVoucherId(@Param("userId") Integer userId, @Param("voucherId") Integer voucherId);
}

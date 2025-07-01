package org.datn.bookstation.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.datn.bookstation.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;

public interface UserRepository extends JpaRepository<User, Integer> {
    ScopedValue<Object> findByEmail(@Size(max = 100) @NotNull String email, Sort sort);
    // Có thể bổ sung các phương thức tìm kiếm custom nếu cần
}


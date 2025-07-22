package org.datn.bookstation.repository;

import jakarta.validation.constraints.Size;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.RoleName;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    List<User> findByRole_RoleName(RoleName roleName);

    @Query("""
            select new org.datn.bookstation.dto.request.UserRoleRequest(u.id, u.fullName, u.phoneNumber, u.role.id)
            from User u
            where u.role.id = 3
            and (
               lower(u.fullName) like lower(concat('%', :text, '%'))
                or u.phoneNumber like concat('%', :text, '%')
            )
            """)
    List<UserRoleRequest> getUserByIdRole(@Param("text") String text);

    User getByPhoneNumber(@Size(max = 20) String phoneNumber);
   

}

package org.datn.bookstation.controller;

import org.datn.bookstation.entity.User;
import org.datn.bookstation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    // Lấy tất cả user
    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userService.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", u.getId());
            map.put("full_name", u.getFullName());
            map.put("email", u.getEmail());
            map.put("phone_number", u.getPhoneNumber());
            map.put("role_id", u.getRole() != null ? u.getRole().getId() : null);
            map.put("status", u.getStatus());
            map.put("created_at", u.getCreatedAt());
            map.put("updated_at", u.getUpdatedAt());
            map.put("total_spent", u.getTotalSpent() != null ? u.getTotalSpent() : BigDecimal.ZERO);
            map.put("total_point", u.getTotalPoint() != null ? u.getTotalPoint() : 0);
            result.add(map);
        }
        return result;
    }

    // Lấy user theo id
    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Integer id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", u.getId());
            map.put("full_name", u.getFullName());
            map.put("email", u.getEmail());
            map.put("phone_number", u.getPhoneNumber());
            map.put("role_id", u.getRole() != null ? u.getRole().getId() : null);
            map.put("status", u.getStatus());
            map.put("created_at", u.getCreatedAt());
            map.put("updated_at", u.getUpdatedAt());
            map.put("total_spent", u.getTotalSpent() != null ? u.getTotalSpent() : BigDecimal.ZERO);
            map.put("total_point", u.getTotalPoint() != null ? u.getTotalPoint() : 0);
            return map;
        }
        throw new RuntimeException("User not found");
    }

    // Thêm user mới
    @PostMapping
    public Map<String, Object> createUser(@RequestBody Map<String, Object> req) {
        User user = new User();
        user.setFullName((String) req.get("full_name"));
        user.setEmail((String) req.get("email"));
        user.setPhoneNumber((String) req.get("phone_number"));
        user.setStatus(Byte.valueOf(req.getOrDefault("status", "1").toString()));
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        user.setTotalSpent(new BigDecimal(req.getOrDefault("total_spent", 0).toString()));
        user.setTotalPoint((Integer) req.getOrDefault("total_point", 0));
        // TODO: set role theo role_id nếu cần
        // user.setRole(...);

        User saved = userService.save(user);
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", saved.getId());
        map.put("full_name", saved.getFullName());
        map.put("email", saved.getEmail());
        map.put("phone_number", saved.getPhoneNumber());
        map.put("role_id", saved.getRole() != null ? saved.getRole().getId() : null);
        map.put("status", saved.getStatus());
        map.put("created_at", saved.getCreatedAt());
        map.put("updated_at", saved.getUpdatedAt());
        map.put("total_spent", saved.getTotalSpent() != null ? saved.getTotalSpent() : BigDecimal.ZERO);
        map.put("total_point", saved.getTotalPoint() != null ? saved.getTotalPoint() : 0);
        return map;
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFullName((String) req.get("full_name"));
            user.setEmail((String) req.get("email"));
            user.setPhoneNumber((String) req.get("phone_number"));
            user.setStatus(Byte.valueOf(req.getOrDefault("status", "1").toString()));
            user.setUpdatedAt(System.currentTimeMillis());
            user.setTotalSpent(new BigDecimal(req.getOrDefault("total_spent", 0).toString()));
            user.setTotalPoint((Integer) req.getOrDefault("total_point", 0));
            // TODO: set role theo role_id nếu cần
            // user.setRole(...);

            User saved = userService.save(user);
            Map<String, Object> map = new HashMap<>();
            map.put("user_id", saved.getId());
            map.put("full_name", saved.getFullName());
            map.put("email", saved.getEmail());
            map.put("phone_number", saved.getPhoneNumber());
            map.put("role_id", saved.getRole() != null ? saved.getRole().getId() : null);
            map.put("status", saved.getStatus());
            map.put("created_at", saved.getCreatedAt());
            map.put("updated_at", saved.getUpdatedAt());
            map.put("total_spent", saved.getTotalSpent() != null ? saved.getTotalSpent() : BigDecimal.ZERO);
            map.put("total_point", saved.getTotalPoint() != null ? saved.getTotalPoint() : 0);
            return map;
        }
        throw new RuntimeException("User not found");
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
    }
}

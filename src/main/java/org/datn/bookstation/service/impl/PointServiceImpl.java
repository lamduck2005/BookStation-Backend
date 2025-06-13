package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Point;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.repository.PointRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.PointService;
import org.springframework.stereotype.Service;
import org.datn.bookstation.dto.request.PointRequest;
import org.datn.bookstation.mapper.PointMapper;
import org.datn.bookstation.dto.response.ApiResponse;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class PointServiceImpl implements PointService {
    private final PointRepository pointRepository;
    private final PointMapper pointMapper;
    private final UserRepository userRepository;

    @Override
    public List<Point> getAll() {
        return pointRepository.findAll();
    }

    @Override
    public Point getById(Integer id) {
        return pointRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<Point> add(PointRequest pointRequest) {
        if (pointRequest.getEmail() == null || pointRequest.getEmail().isEmpty()) {
            return new ApiResponse<>(404, "Email is required", null);
        }
        User user = userRepository.findByEmail(pointRequest.getEmail()).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "User with email does not exist", null);
        }
        Point point = new Point();
        point.setUser(user);
        point.setOrderId(pointRequest.getOrder()); // cần đảm bảo entity Point có trường orderId kiểu Long
        point.setPointEarned(pointRequest.getPointEarned());
        point.setMinSpent(pointRequest.getMinSpent());
        point.setPointSpent(pointRequest.getPointSpent());
        point.setDescription(pointRequest.getDescription());
        point.setStatus(pointRequest.getStatus());
        point.setCreatedAt(java.time.Instant.now());
        Point saved = pointRepository.save(point);
        return new ApiResponse<>(201, "Created", saved);
    }

    @Override
    public Point update(Point point, Integer id) {
        Point existing = pointRepository.findById(id).orElseThrow(() -> new RuntimeException("Point not found"));
        point.setId(id);
        point.setCreatedAt(existing.getCreatedAt());
        point.setUpdatedAt(Instant.now());
        return pointRepository.save(point);
    }

    @Override
    public void delete(Integer id) {
        pointRepository.deleteById(id);
    }
}

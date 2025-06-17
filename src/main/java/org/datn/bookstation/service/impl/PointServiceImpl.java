package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.Point;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.mapper.PointMapper;
import org.datn.bookstation.repository.PointRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.PointService;
import org.springframework.stereotype.Service;
import org.datn.bookstation.dto.request.PointRequest;
import org.datn.bookstation.dto.response.ApiResponse;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class PointServiceImpl implements PointService {
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final PointMapper pointMapper;

    @Override
    public List<Point> getAll() {
        return pointRepository.findAll();
    }    @Override
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
        
        Point point = pointMapper.toPoint(pointRequest);
        point.setUser(user);
        
        // Set order if orderId is provided
        if (pointRequest.getOrderId() != null) {
            Order order = new Order();
            order.setId(pointRequest.getOrderId());
            point.setOrder(order);
        }
        
        // Update user's total points
        int currentTotalPoints = user.getTotalPoint() != null ? user.getTotalPoint() : 0;
        
        // Add points earned (if any)
        if (pointRequest.getPointEarned() != null && pointRequest.getPointEarned() > 0) {
            currentTotalPoints += pointRequest.getPointEarned();
        }
        
        // Subtract points spent (if any)
        if (pointRequest.getPointSpent() != null && pointRequest.getPointSpent() > 0) {
            currentTotalPoints -= pointRequest.getPointSpent();
            // Ensure total points don't go below 0
            if (currentTotalPoints < 0) {
                return new ApiResponse<>(400, "Insufficient points. User only has " + user.getTotalPoint() + " points", null);
            }        }
        
        // Update user's total points
        user.setTotalPoint(currentTotalPoints);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
          point.setCreatedAt(java.time.Instant.now());
        Point saved = pointRepository.save(point);
        return new ApiResponse<>(201, "Created", saved);
    }    @Override
    public ApiResponse<Point> update(PointRequest pointRequest, Integer id) {
        // Validate input
        if (pointRequest.getEmail() == null || pointRequest.getEmail().isEmpty()) {
            return new ApiResponse<>(400, "Email is required", null);
        }
        
        // Find existing point record
        Point existingPoint = pointRepository.findById(id).orElse(null);
        if (existingPoint == null) {
            return new ApiResponse<>(404, "Point record not found", null);
        }
        
        // Find user by email
        User user = userRepository.findByEmail(pointRequest.getEmail()).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "User with email does not exist", null);        }
        
        // Calculate the difference in points to adjust user's total points
        int currentTotalPoints = user.getTotalPoint() != null ? user.getTotalPoint() : 0;
        
        // Revert old point changes
        if (existingPoint.getPointEarned() != null && existingPoint.getPointEarned() > 0) {
            currentTotalPoints -= existingPoint.getPointEarned();
        }
        if (existingPoint.getPointSpent() != null && existingPoint.getPointSpent() > 0) {
            currentTotalPoints += existingPoint.getPointSpent();
        }
        
        // Apply new point changes
        if (pointRequest.getPointEarned() != null && pointRequest.getPointEarned() > 0) {
            currentTotalPoints += pointRequest.getPointEarned();
        }
        if (pointRequest.getPointSpent() != null && pointRequest.getPointSpent() > 0) {
            currentTotalPoints -= pointRequest.getPointSpent();
            // Ensure total points don't go below 0
            if (currentTotalPoints < 0) {
                return new ApiResponse<>(400, "Insufficient points. User would have " + currentTotalPoints + " points after this update", null);
            }        }
        
        // Update point entity with new data
        Point updatedPoint = pointMapper.toPoint(pointRequest);
        updatedPoint.setId(id);
        updatedPoint.setUser(user);
        updatedPoint.setCreatedAt(existingPoint.getCreatedAt());
        updatedPoint.setUpdatedAt(Instant.now());
        
        // Set order if orderId is provided
        if (pointRequest.getOrderId() != null) {
            Order order = new Order();
            order.setId(pointRequest.getOrderId());
            updatedPoint.setOrder(order);        }
        
        // Update user's total points
        user.setTotalPoint(currentTotalPoints);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        
        // Save updated point record
        Point saved = pointRepository.save(updatedPoint);
        return new ApiResponse<>(200, "Updated successfully", saved);
    }    @Override
    public void delete(Integer id) {
        // Find existing point record to revert point changes
        Point existingPoint = pointRepository.findById(id).orElse(null);
        if (existingPoint != null && existingPoint.getUser() != null) {
            User user = existingPoint.getUser();
            
            // Revert point changes
            int currentTotalPoints = user.getTotalPoint() != null ? user.getTotalPoint() : 0;
            
            if (existingPoint.getPointEarned() != null && existingPoint.getPointEarned() > 0) {
                currentTotalPoints -= existingPoint.getPointEarned();
            }
            if (existingPoint.getPointSpent() != null && existingPoint.getPointSpent() > 0) {
                currentTotalPoints += existingPoint.getPointSpent();
            }
            
            // Ensure total points don't go below 0
            if (currentTotalPoints < 0) {
                currentTotalPoints = 0;
            }
            
            // Update user
            user.setTotalPoint(currentTotalPoints);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        }
        
        pointRepository.deleteById(id);
    }
}

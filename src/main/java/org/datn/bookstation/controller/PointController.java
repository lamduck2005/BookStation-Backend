package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.PointRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Point;
import org.datn.bookstation.service.PointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Point>>> getAll() {
        List<Point> points = pointService.getAll();
        ApiResponse<List<Point>> response = new ApiResponse<>(HttpStatus.OK.value(), "Success", points);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Point>> getById(@PathVariable Integer id) {
        Point point = pointService.getById(id);
        if (point == null) {
            ApiResponse<Point> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Point> response = new ApiResponse<>(HttpStatus.OK.value(), "Success", point);
        return ResponseEntity.ok(response);
    }    @PostMapping
    public ResponseEntity<ApiResponse<Point>> add(@RequestBody PointRequest pointRequest) {
        ApiResponse<Point> response = pointService.add(pointRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } 
       @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Point>> update(@PathVariable Integer id, @RequestBody PointRequest pointRequest) {
        ApiResponse<Point> response = pointService.update(pointRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        pointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

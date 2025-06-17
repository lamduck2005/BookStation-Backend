package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.service.RankService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ranks")
public class RankController {
    private final RankService rankService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Rank>>> getAll() {
        List<Rank> ranks = rankService.getAll();
        ApiResponse<List<Rank>> response = new ApiResponse<>(HttpStatus.OK.value(), "Success", ranks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Rank>> getById(@PathVariable Integer id) {
        Rank rank = rankService.getById(id);
        if (rank == null) {
            ApiResponse<Rank> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Not found", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<Rank> response = new ApiResponse<>(HttpStatus.OK.value(), "Success", rank);
        return ResponseEntity.ok(response);
    }

    @PostMapping    public ResponseEntity<ApiResponse<Rank>> add(@RequestBody RankRequest rankRequest) {
        ApiResponse<Rank> response = rankService.add(rankRequest);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Rank>> update(@PathVariable Integer id, @RequestBody Rank rank) {
        Rank updated = rankService.update(rank, id);
        ApiResponse<Rank> response = new ApiResponse<>(HttpStatus.OK.value(), "Updated", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        rankService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Integer> {
}

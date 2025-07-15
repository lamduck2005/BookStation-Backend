package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PointRepository extends JpaRepository<Point, Integer>, JpaSpecificationExecutor<Point> {

    Point getByUserId(Integer userId);
}

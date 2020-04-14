package com.example.airquality.repository;

import com.example.airquality.entity.Location;
import com.example.airquality.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface ReportRepository extends JpaRepository<Report, Location> {

    @Override
    Optional<Report> findById(Location location);

    @Override
    boolean existsById(Location location);

}

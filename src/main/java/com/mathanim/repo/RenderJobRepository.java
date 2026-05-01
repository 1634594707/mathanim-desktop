package com.mathanim.repo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mathanim.domain.JobStatus;
import com.mathanim.domain.RenderJob;

public interface RenderJobRepository extends JpaRepository<RenderJob, UUID> {

  List<RenderJob> findAllByOrderByCreatedAtDesc();

  Page<RenderJob> findAllByOrderByCreatedAtDesc(Pageable pageable);

  List<RenderJob> findAllByStatus(JobStatus status);

  Page<RenderJob> findAllByStatus(JobStatus status, Pageable pageable);

  List<RenderJob> findAllByFavoritedTrueOrderByCreatedAtDesc();

  Page<RenderJob> findAllByFavoritedTrueOrderByCreatedAtDesc(Pageable pageable);

  @Query("SELECT j FROM RenderJob j WHERE " +
         "LOWER(j.concept) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
         "ORDER BY j.createdAt DESC")
  List<RenderJob> searchByKeyword(@Param("keyword") String keyword);

  @Query("SELECT j FROM RenderJob j WHERE " +
         "LOWER(j.concept) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
         "ORDER BY j.createdAt DESC")
  Page<RenderJob> searchByKeywordPaged(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT j FROM RenderJob j WHERE " +
         "j.status = :status AND " +
         "LOWER(j.concept) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
         "ORDER BY j.createdAt DESC")
  List<RenderJob> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") JobStatus status);

  @Query("SELECT j FROM RenderJob j WHERE " +
         "j.status = :status AND " +
         "LOWER(j.concept) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
         "ORDER BY j.createdAt DESC")
  Page<RenderJob> searchByKeywordAndStatusPaged(@Param("keyword") String keyword, @Param("status") JobStatus status, Pageable pageable);

  long countByStatus(JobStatus status);

  @Query("SELECT COUNT(j) FROM RenderJob j WHERE j.fallbackModeActive = true")
  long countByFallbackModeActive();

  @Modifying
  long deleteByStatusAndCreatedAtBefore(JobStatus status, Instant createdAt);
}


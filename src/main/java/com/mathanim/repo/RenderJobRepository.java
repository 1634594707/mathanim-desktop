package com.mathanim.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mathanim.domain.JobStatus;
import com.mathanim.domain.RenderJob;

public interface RenderJobRepository extends JpaRepository<RenderJob, UUID> {

  List<RenderJob> findAllByOrderByCreatedAtDesc();

  List<RenderJob> findAllByStatus(JobStatus status);

  List<RenderJob> findAllByFavoritedTrueOrderByCreatedAtDesc();

  @Query("SELECT j FROM RenderJob j WHERE " +
         "LOWER(j.concept) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
         "ORDER BY j.createdAt DESC")
  List<RenderJob> searchByKeyword(@Param("keyword") String keyword);

  @Query("SELECT j FROM RenderJob j WHERE " +
         "j.status = :status AND " +
         "LOWER(j.concept) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
         "ORDER BY j.createdAt DESC")
  List<RenderJob> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") JobStatus status);
}


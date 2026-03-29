package com.mathanim.repo;

import com.mathanim.domain.JobStatus;
import com.mathanim.domain.RenderJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RenderJobRepository extends JpaRepository<RenderJob, UUID> {

  List<RenderJob> findAllByOrderByCreatedAtDesc();

  List<RenderJob> findAllByStatus(JobStatus status);
}

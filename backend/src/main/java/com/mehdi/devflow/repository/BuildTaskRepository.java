package com.mehdi.devflow.repository;

import com.mehdi.devflow.entity.BuildTask;
import com.mehdi.devflow.enums.BuildStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuildTaskRepository extends JpaRepository<BuildTask, UUID> {

    List<BuildTask> findByStatus(BuildStatus status);

    List<BuildTask> findByBranch(String branch);

    List<BuildTask> findByRepositoryUrl(String repositoryUrl);

}

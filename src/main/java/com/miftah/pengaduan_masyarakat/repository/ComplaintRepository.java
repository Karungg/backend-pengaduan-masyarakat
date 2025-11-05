package com.miftah.pengaduan_masyarakat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miftah.pengaduan_masyarakat.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    boolean existsByCategoryId(UUID categoryId);

}

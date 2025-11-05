package com.miftah.pengaduan_masyarakat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miftah.pengaduan_masyarakat.model.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    boolean existsByCategoryId(UUID categoryId);

}

package com.miftah.pengaduan_masyarakat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miftah.pengaduan_masyarakat.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

}

package com.teklif.app.repository;

import com.teklif.app.entity.ProductFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFileRepository extends JpaRepository<ProductFile, String> {

    List<ProductFile> findByProductIdAndIsDeletedFalse(String productId);

    @Query("SELECT pf FROM ProductFile pf " +
            "LEFT JOIN FETCH pf.product " +
            "WHERE pf.productId = :productId AND pf.isDeleted = false")
    List<ProductFile> findByProductIdWithFetch(@Param("productId") String productId);

    List<ProductFile> findByProductIdAndCategoryAndIsDeletedFalse(String productId, ProductFile.FileType category);

    void deleteByProductId(String productId);

    long countByProductIdAndIsDeletedFalse(String productId);
}

package bk.dao;

import bk.entity.Technology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TechnologyDAO {

    Technology save(Technology technology);

    Technology update(Technology technology);

    List<Technology> findAllActive();

    Page<Technology> findAllActive(Pageable pageable);

    Optional<Technology> findByIdAndActive(Integer id);

    boolean existsByNameAndNotId(String name, Integer id);

    boolean existsByNameAndActive(String name);

    Page<Technology> searchByName(String keyword, Pageable pageable);

    long countActive();

    Page<Technology> findAllDeleted(Pageable pageable);

    void restoreById(Integer id);

    void softDeleteById(Integer id);

    void hardDeleteById(Integer id);

    Optional<Technology> findByIdIncludeDeleted(Integer id);

    List<Technology> findAll();

    long count();
}
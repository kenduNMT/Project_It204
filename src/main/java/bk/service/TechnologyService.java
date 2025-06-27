package bk.service;

import bk.entity.Technology;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TechnologyService {
    List<Technology> getAllActiveTechnologies();
    Page<Technology> getAllActiveTechnologies(int page, int size);
    Optional<Technology> findById(Integer id);

    void createTechnology(Technology technology);
    void updateTechnology(Integer id, Technology updatedTechnology);
    void softDeleteTechnology(Integer id);
    void restoreTechnology(Integer id);
    void permanentDeleteTechnology(Integer id);
    Page<Technology> searchTechnologies(String keyword, int page, int size);

    Page<Technology> getDeletedTechnologies(int page, int size);
    boolean isNameExists(String name);
    boolean isNameExistsExceptId(String name, Integer id);

    List<Technology> findAllActive();

    long getTotalCount();
}

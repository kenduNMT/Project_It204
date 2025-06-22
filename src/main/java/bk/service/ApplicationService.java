package bk.service;

import bk.entity.Application;
import java.util.List;

public interface ApplicationService {
    List<Application> findAll();
    Application findById(Long id);
    void save(Application application);
    void update(Application application);
    void delete(Long id);
    List<Application> findByStatus(Application.Status status);
    
    // New methods for pagination, filter and search
    List<Application> findAllWithPagination(int page, int size);
    List<Application> findByStatusWithPagination(Application.Status status, int page, int size);
    List<Application> searchWithPagination(String searchTerm, int page, int size);
    List<Application> findByStatusAndSearchWithPagination(Application.Status status, String searchTerm, int page, int size);
    long countAll();
    long countByStatus(Application.Status status);
    long countBySearch(String searchTerm);
    long countByStatusAndSearch(Application.Status status, String searchTerm);
}
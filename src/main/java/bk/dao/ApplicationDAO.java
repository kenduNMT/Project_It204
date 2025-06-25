package bk.dao;

import bk.entity.Application;
import java.util.List;

public interface ApplicationDAO {
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
    boolean hasApplied(Integer candidateId, Integer recruitmentPositionId);
    List<Application> findByCandidateId(Integer candidateId, int page, int size);
    long countByCandidateId(Integer candidateId);
    long countByCandidateIdAndStatus(Integer candidateId, Application.Status status);
    List<Application> findByCandidateIdAndStatus(Integer candidateId, Application.Status status, int page, int size);
}
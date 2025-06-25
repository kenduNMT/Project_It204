package bk.service.impl;

import bk.dao.ApplicationDAO;
import bk.entity.Application;
import bk.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    private ApplicationDAO applicationDAO;

    @Override
    public List<Application> findAll() {
        return applicationDAO.findAll();
    }

    @Override
    public Application findById(Long id) {
        return applicationDAO.findById(id);
    }

    @Override
    public Application save(Application application) {
        applicationDAO.save(application);
        return application;
    }

    @Override
    public void update(Application application) {
        applicationDAO.update(application);
    }

    @Override
    public void delete(Long id) {
        applicationDAO.delete(id);
    }

    @Override
    public List<Application> findByStatus(Application.Status status) {
        return applicationDAO.findByStatus(status);
    }

    // New methods for pagination, filter and search
    @Override
    public List<Application> findAllWithPagination(int page, int size) {
        return applicationDAO.findAllWithPagination(page, size);
    }

    @Override
    public List<Application> findByStatusWithPagination(Application.Status status, int page, int size) {
        return applicationDAO.findByStatusWithPagination(status, page, size);
    }

    @Override
    public List<Application> searchWithPagination(String searchTerm, int page, int size) {
        return applicationDAO.searchWithPagination(searchTerm, page, size);
    }

    @Override
    public List<Application> findByStatusAndSearchWithPagination(Application.Status status, String searchTerm, int page, int size) {
        return applicationDAO.findByStatusAndSearchWithPagination(status, searchTerm, page, size);
    }

    @Override
    public long countAll() {
        return applicationDAO.countAll();
    }

    @Override
    public long countByStatus(Application.Status status) {
        return applicationDAO.countByStatus(status);
    }

    @Override
    public long countBySearch(String searchTerm) {
        return applicationDAO.countBySearch(searchTerm);
    }

    @Override
    public long countByStatusAndSearch(Application.Status status, String searchTerm) {
        return applicationDAO.countByStatusAndSearch(status, searchTerm);
    }

    @Override
    public boolean hasApplied(Integer candidateId, Integer recruitmentPositionId) {
        try {
            return applicationDAO.hasApplied(candidateId, recruitmentPositionId);
        } catch (Exception e) {
            // Log the error and return false as safe fallback
            System.err.println("Error checking if candidate has applied: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Application> findByCandidateId(Integer candidateId, int page, int size) {
        return applicationDAO.findByCandidateId(candidateId, page, size);
    }

    @Override
    public long countByCandidateId(Integer candidateId) {
        return applicationDAO.countByCandidateId(candidateId);
    }

    @Override
    public long countByCandidateIdAndStatus(Integer candidateId, Application.Status status) {
        return applicationDAO.countByCandidateIdAndStatus(candidateId, status);
    }

    @Override
    public List<Application> findByCandidateIdAndStatus(Integer candidateId, Application.Status status, int page, int size) {
        return applicationDAO.findByCandidateIdAndStatus(candidateId, status, page, size);
    }
}
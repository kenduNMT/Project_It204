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
    public void save(Application application) {
        applicationDAO.save(application);
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
}
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
}
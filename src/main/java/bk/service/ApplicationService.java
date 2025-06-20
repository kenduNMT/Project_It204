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
}
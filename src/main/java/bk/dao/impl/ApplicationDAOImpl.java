package bk.dao.impl;

import bk.dao.ApplicationDAO;
import bk.entity.Application;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {
    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Application> findAll() {
        String hql = "SELECT a FROM Application a " +
                "LEFT JOIN FETCH a.candidate " +
                "LEFT JOIN FETCH a.recruitmentPosition " +
                "ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class).list();
    }

    @Override
    public Application findById(Long id) {
        return getSession().get(Application.class, id);
    }

    @Override
    public void save(Application application) {
        getSession().save(application);
    }

    @Override
    public void update(Application application) {
        getSession().update(application);
    }

    @Override
    public void delete(Long id) {
        Application app = findById(id);
        if (app != null) getSession().delete(app);
    }

    @Override
    public List<Application> findByStatus(Application.Status status) {
        return getSession().createQuery("FROM Application a WHERE a.status = :status", Application.class)
                .setParameter("status", status)
                .list();
    }
}
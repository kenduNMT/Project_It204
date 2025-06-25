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

    // New methods for pagination, filter and search
    @Override
    public List<Application> findAllWithPagination(int page, int size) {
        String hql = "SELECT a FROM Application a " +
                "LEFT JOIN FETCH a.candidate " +
                "LEFT JOIN FETCH a.recruitmentPosition " +
                "ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .list();
    }

    @Override
    public List<Application> findByStatusWithPagination(Application.Status status, int page, int size) {
        String hql = "SELECT a FROM Application a " +
                "LEFT JOIN FETCH a.candidate " +
                "LEFT JOIN FETCH a.recruitmentPosition " +
                "WHERE a.status = :status " +
                "ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class)
                .setParameter("status", status)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .list();
    }

    @Override
    public List<Application> searchWithPagination(String searchTerm, int page, int size) {
        String hql = "SELECT a FROM Application a " +
                "LEFT JOIN FETCH a.candidate c " +
                "LEFT JOIN FETCH a.recruitmentPosition r " +
                "WHERE LOWER(c.name) LIKE LOWER(:searchTerm) " +
                "OR LOWER(r.name) LIKE LOWER(:searchTerm) " +
                "ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .list();
    }

    @Override
    public List<Application> findByStatusAndSearchWithPagination(Application.Status status, String searchTerm, int page, int size) {
        String hql = "SELECT a FROM Application a " +
                "LEFT JOIN FETCH a.candidate c " +
                "LEFT JOIN FETCH a.recruitmentPosition r " +
                "WHERE a.status = :status " +
                "AND (LOWER(c.name) LIKE LOWER(:searchTerm) " +
                "OR LOWER(r.name) LIKE LOWER(:searchTerm)) " +
                "ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class)
                .setParameter("status", status)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .list();
    }

    @Override
    public long countAll() {
        return getSession().createQuery("SELECT COUNT(a) FROM Application a", Long.class).uniqueResult();
    }

    @Override
    public long countByStatus(Application.Status status) {
        return getSession().createQuery("SELECT COUNT(a) FROM Application a WHERE a.status = :status", Long.class)
                .setParameter("status", status)
                .uniqueResult();
    }

    @Override
    public long countBySearch(String searchTerm) {
        String hql = "SELECT COUNT(a) FROM Application a " +
                "LEFT JOIN a.candidate c " +
                "LEFT JOIN a.recruitmentPosition r " +
                "WHERE LOWER(c.name) LIKE LOWER(:searchTerm) " +
                "OR LOWER(r.name) LIKE LOWER(:searchTerm)";
        return getSession().createQuery(hql, Long.class)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .uniqueResult();
    }

    @Override
    public long countByStatusAndSearch(Application.Status status, String searchTerm) {
        String hql = "SELECT COUNT(a) FROM Application a " +
                "LEFT JOIN a.candidate c " +
                "LEFT JOIN a.recruitmentPosition r " +
                "WHERE a.status = :status " +
                "AND (LOWER(c.name) LIKE LOWER(:searchTerm) " +
                "OR LOWER(r.name) LIKE LOWER(:searchTerm))";
        return getSession().createQuery(hql, Long.class)
                .setParameter("status", status)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .uniqueResult();
    }

    @Override
    public boolean hasApplied(Integer candidateId, Integer recruitmentPositionId) {
        try {
            String hql = "SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId AND a.recruitmentPosition.id = :recruitmentPositionId";
            Long count = getSession().createQuery(hql, Long.class)
                    .setParameter("candidateId", candidateId)
                    .setParameter("recruitmentPositionId", recruitmentPositionId)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            // Alternative approach using native SQL to avoid type casting issues
            System.err.println("HQL query failed, trying native SQL approach: " + e.getMessage());
            try {
                String sql = "SELECT COUNT(*) FROM applications a WHERE a.candidate_id = :candidateId AND a.recruitment_position_id = :recruitmentPositionId";
                Long count = (Long) getSession().createNativeQuery(sql)
                        .setParameter("candidateId", candidateId)
                        .setParameter("recruitmentPositionId", recruitmentPositionId)
                        .uniqueResult();
                return count != null && count > 0;
            } catch (Exception e2) {
                System.err.println("Native SQL query also failed: " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public List<Application> findByCandidateId(Integer candidateId, int page, int size) {
        String hql = "FROM Application a WHERE a.candidate.id = :candidateId ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class)
                .setParameter("candidateId", candidateId)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .list();
    }

    @Override
    public long countByCandidateId(Integer candidateId) {
        String hql = "SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId";
        return getSession().createQuery(hql, Long.class)
                .setParameter("candidateId", candidateId)
                .uniqueResult();
    }

    @Override
    public long countByCandidateIdAndStatus(Integer candidateId, Application.Status status) {
        String hql = "SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId AND a.status = :status";
        return getSession().createQuery(hql, Long.class)
                .setParameter("candidateId", candidateId)
                .setParameter("status", status)
                .uniqueResult();
    }

    @Override
    public List<Application> findByCandidateIdAndStatus(Integer candidateId, Application.Status status, int page, int size) {
        String hql = "FROM Application a WHERE a.candidate.id = :candidateId AND a.status = :status ORDER BY a.createdAt DESC";
        return getSession().createQuery(hql, Application.class)
                .setParameter("candidateId", candidateId)
                .setParameter("status", status)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .list();
    }
}
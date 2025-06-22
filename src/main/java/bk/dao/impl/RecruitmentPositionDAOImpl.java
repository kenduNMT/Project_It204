package bk.dao.impl;

import bk.dao.RecruitmentPositionDAO;
import bk.entity.RecruitmentPosition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

@Repository
public class RecruitmentPositionDAOImpl implements RecruitmentPositionDAO {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<RecruitmentPosition> findAll() {
        try {
            String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false ORDER BY rp.createdDate DESC";
            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecruitmentPosition> findAll(int page, int size) {
        try {
            String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false ORDER BY rp.createdDate DESC";
            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecruitmentPosition> findByName(String name, int page, int size) {
        try {
            String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.name LIKE :name AND rp.isDeleted = false ORDER BY rp.createdDate DESC";
            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
            query.setParameter("name", "%" + name + "%");
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecruitmentPosition> findActivePositions(int page, int size) {
        try {
            String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false AND (rp.expiredDate IS NULL OR rp.expiredDate >= CURRENT_DATE) ORDER BY rp.createdDate DESC";
            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecruitmentPosition> findExpiredPositions(int page, int size) {
        try {
            String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false AND rp.expiredDate < CURRENT_DATE ORDER BY rp.expiredDate DESC";
            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public RecruitmentPosition findById(Integer id) {
        try {
            String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.id = :id AND rp.isDeleted = false";
            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void save(RecruitmentPosition recruitmentPosition) {
        getCurrentSession().saveOrUpdate(recruitmentPosition);
    }

    @Override
    public void update(RecruitmentPosition recruitmentPosition) {
        getCurrentSession().merge(recruitmentPosition);
    }

    @Override
    public void delete(Integer id) {
        RecruitmentPosition recruitmentPosition = findById(id);
        if (recruitmentPosition != null) {
            recruitmentPosition.softDelete();
            getCurrentSession().merge(recruitmentPosition);
        }
    }

    @Override
    public void hardDelete(Integer id) {
        RecruitmentPosition recruitmentPosition = getCurrentSession().get(RecruitmentPosition.class, id);
        if (recruitmentPosition != null) {
            getCurrentSession().delete(recruitmentPosition);
        }
    }

    @Override
    public List<RecruitmentPosition> findByName(String name) {
        String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.name LIKE :name AND rp.isDeleted = false ORDER BY rp.createdDate DESC";
        Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    @Override
    public List<RecruitmentPosition> findActivePositions() {
        String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false AND (rp.expiredDate IS NULL OR rp.expiredDate >= CURRENT_DATE) ORDER BY rp.createdDate DESC";
        Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
        return query.getResultList();
    }

    @Override
    public List<RecruitmentPosition> findExpiredPositions() {
        String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false AND rp.expiredDate < CURRENT_DATE ORDER BY rp.expiredDate DESC";
        Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
        return query.getResultList();
    }

    @Override
    public Long countAll() {
        String hql = "SELECT COUNT(rp) FROM RecruitmentPosition rp WHERE rp.isDeleted = false";
        Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
        return query.getSingleResult();
    }

    @Override
    public Long countActive() {
        String hql = "SELECT COUNT(rp) FROM RecruitmentPosition rp WHERE rp.isDeleted = false AND (rp.expiredDate IS NULL OR rp.expiredDate >= CURRENT_DATE)";
        Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
        return query.getSingleResult();
    }

    @Override
    public Long countByName(String name) {
        String hql = "SELECT COUNT(rp) FROM RecruitmentPosition rp WHERE rp.name LIKE :name AND rp.isDeleted = false";
        Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
        query.setParameter("name", "%" + name + "%");
        return query.getSingleResult();
    }
}
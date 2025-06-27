package bk.dao.impl;

import bk.dao.TechnologyDAO;
import bk.entity.Technology;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TechnologyDAOImpl implements TechnologyDAO {

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Lấy Session hiện tại từ SessionFactory.
     * @return Session hiện tại.
     */
    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Technology save(Technology technology) {
        getCurrentSession().save(technology);
        return technology;
    }

    @Override
    public Technology update(Technology technology) {
        getCurrentSession().update(technology);
        return technology;
    }

    @Override
    public List<Technology> findAllActive() {
        try {
            String hql = "FROM Technology t WHERE t.isDeleted = false ORDER BY t.name ASC";
            Query<Technology> query = getCurrentSession().createQuery(hql, Technology.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Page<Technology> findAllActive(Pageable pageable) {
        try {
            // Count query
            String countHql = "SELECT COUNT(t) FROM Technology t WHERE t.isDeleted = false";
            Query<Long> countQuery = getCurrentSession().createQuery(countHql, Long.class);
            Long totalElements = countQuery.uniqueResult();

            // Data query
            String hql = "FROM Technology t WHERE t.isDeleted = false ORDER BY t.name ASC";
            Query<Technology> query = getCurrentSession().createQuery(hql, Technology.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Technology> content = query.list();
            return new PageImpl<>(content, pageable, totalElements != null ? totalElements : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    @Override
    public Optional<Technology> findByIdAndActive(Integer id) {
        try {
            String hql = "FROM Technology t WHERE t.id = :id AND t.isDeleted = false";
            Query<Technology> query = getCurrentSession().createQuery(hql, Technology.class);
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByNameAndNotId(String name, Integer id) {
        try {
            String hql = "SELECT COUNT(t) FROM Technology t WHERE t.name = :name AND t.id != :id AND t.isDeleted = false";
            Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
            query.setParameter("name", name);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsByNameAndActive(String name) {
        try {
            String hql = "SELECT COUNT(t) FROM Technology t WHERE t.name = :name AND t.isDeleted = false";
            Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
            query.setParameter("name", name);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Page<Technology> searchByName(String keyword, Pageable pageable) {
        try {
            // Count query
            String countHql = "SELECT COUNT(t) FROM Technology t WHERE LOWER(t.name) LIKE LOWER(:keyword) AND t.isDeleted = false";
            Query<Long> countQuery = getCurrentSession().createQuery(countHql, Long.class);
            countQuery.setParameter("keyword", "%" + keyword + "%");
            Long totalElements = countQuery.uniqueResult();

            // Data query
            String hql = "FROM Technology t WHERE LOWER(t.name) LIKE LOWER(:keyword) AND t.isDeleted = false ORDER BY t.name ASC";
            Query<Technology> query = getCurrentSession().createQuery(hql, Technology.class);
            query.setParameter("keyword", "%" + keyword + "%");
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Technology> content = query.list();
            return new PageImpl<>(content, pageable, totalElements != null ? totalElements : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    @Override
    public long countActive() {
        try {
            String hql = "SELECT COUNT(t) FROM Technology t WHERE t.isDeleted = false";
            Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
            Long count = query.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Page<Technology> findAllDeleted(Pageable pageable) {
        try {
            // Count query
            String countHql = "SELECT COUNT(t) FROM Technology t WHERE t.isDeleted = true";
            Query<Long> countQuery = getCurrentSession().createQuery(countHql, Long.class);
            Long totalElements = countQuery.uniqueResult();

            // Data query
            String hql = "FROM Technology t WHERE t.isDeleted = true ORDER BY t.name ASC";
            Query<Technology> query = getCurrentSession().createQuery(hql, Technology.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Technology> content = query.list();
            return new PageImpl<>(content, pageable, totalElements != null ? totalElements : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    @Override
    public void restoreById(Integer id) {
        try {
            String hql = "UPDATE Technology t SET t.isDeleted = false WHERE t.id = :id";
            Query query = getCurrentSession().createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void softDeleteById(Integer id) {
        try {
            String hql = "UPDATE Technology t SET t.isDeleted = true WHERE t.id = :id";
            Query query = getCurrentSession().createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hardDeleteById(Integer id) {
        try {
            String hql = "DELETE FROM Technology t WHERE t.id = :id";
            Query query = getCurrentSession().createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Technology> findByIdIncludeDeleted(Integer id) {
        try {
            Technology technology = getCurrentSession().get(Technology.class, id);
            return Optional.ofNullable(technology);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Technology> findAll() {
        try {
            String hql = "FROM Technology t ORDER BY t.name ASC";
            Query<Technology> query = getCurrentSession().createQuery(hql, Technology.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    public long count() {
        Session session = sessionFactory.getCurrentSession();
        Query<Long> query = session.createQuery("SELECT COUNT(t) FROM Technology t", Long.class);
        return query.getSingleResult();
    }
}
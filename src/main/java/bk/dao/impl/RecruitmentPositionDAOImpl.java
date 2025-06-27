package bk.dao.impl;

import bk.dao.RecruitmentPositionDAO;
import bk.entity.RecruitmentPosition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import java.util.*;

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
    public List<RecruitmentPosition> findByName(String name) {
        String hql = "FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.name LIKE :name AND rp.isDeleted = false ORDER BY rp.createdDate DESC";
        Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql, RecruitmentPosition.class);
        query.setParameter("name", "%" + name + "%");
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

    @Override
    public List<RecruitmentPosition> searchAndFilter(String keyword, String location, String category,
                                                   Double minSalary, Double maxSalary, Integer minExperience,
                                                   String sortBy, int page, int size) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("FROM RecruitmentPosition rp LEFT JOIN FETCH rp.technologies WHERE rp.isDeleted = false ");
            hql.append("AND (rp.expiredDate IS NULL OR rp.expiredDate >= CURRENT_DATE) ");

            Map<String, Object> parameters = new HashMap<>();

            // Add search conditions
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append("AND (rp.name LIKE :keyword OR rp.description LIKE :keyword) ");
                parameters.put("keyword", "%" + keyword.trim() + "%");
            }

            if (location != null && !location.trim().isEmpty()) {
                hql.append("AND rp.location LIKE :location ");
                parameters.put("location", "%" + location.trim() + "%");
            }

            if (category != null && !category.trim().isEmpty()) {
                hql.append("AND rp.category LIKE :category ");
                parameters.put("category", "%" + category.trim() + "%");
            }

            if (minSalary != null) {
                hql.append("AND rp.maxSalary >= :minSalary ");
                parameters.put("minSalary", minSalary);
            }

            if (maxSalary != null) {
                hql.append("AND rp.minSalary <= :maxSalary ");
                parameters.put("maxSalary", maxSalary);
            }

            if (minExperience != null) {
                hql.append("AND rp.minExperience <= :minExperience ");
                parameters.put("minExperience", minExperience);
            }

            // Add sorting
            switch (sortBy) {
                case "salary-high":
                    hql.append("ORDER BY rp.maxSalary DESC");
                    break;
                case "salary-low":
                    hql.append("ORDER BY rp.minSalary ASC");
                    break;
                case "deadline":
                    hql.append("ORDER BY rp.expiredDate ASC");
                    break;
                case "newest":
                default:
                    hql.append("ORDER BY rp.createdDate DESC");
                    break;
            }

            Query<RecruitmentPosition> query = getCurrentSession().createQuery(hql.toString(), RecruitmentPosition.class);
            
            // Set parameters
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public Long countSearchAndFilter(String keyword, String location, String category,
                                   Double minSalary, Double maxSalary, Integer minExperience) {
        try {
            StringBuilder hql = new StringBuilder();
            hql.append("SELECT COUNT(rp) FROM RecruitmentPosition rp WHERE rp.isDeleted = false ");
            hql.append("AND (rp.expiredDate IS NULL OR rp.expiredDate >= CURRENT_DATE) ");

            Map<String, Object> parameters = new HashMap<>();

            // Add search conditions
            if (keyword != null && !keyword.trim().isEmpty()) {
                hql.append("AND (rp.name LIKE :keyword OR rp.description LIKE :keyword) ");
                parameters.put("keyword", "%" + keyword.trim() + "%");
            }

            if (location != null && !location.trim().isEmpty()) {
                hql.append("AND rp.location LIKE :location ");
                parameters.put("location", "%" + location.trim() + "%");
            }

            if (category != null && !category.trim().isEmpty()) {
                hql.append("AND rp.category LIKE :category ");
                parameters.put("category", "%" + category.trim() + "%");
            }

            if (minSalary != null) {
                hql.append("AND rp.maxSalary >= :minSalary ");
                parameters.put("minSalary", minSalary);
            }

            if (maxSalary != null) {
                hql.append("AND rp.minSalary <= :maxSalary ");
                parameters.put("maxSalary", maxSalary);
            }

            if (minExperience != null) {
                hql.append("AND rp.minExperience <= :minExperience ");
                parameters.put("minExperience", minExperience);
            }

            Query<Long> query = getCurrentSession().createQuery(hql.toString(), Long.class);
            
            // Set parameters
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public Map<String, Object> getSalaryRange() {
        try {
            String hql = "SELECT MIN(rp.minSalary), MAX(rp.maxSalary), AVG((rp.minSalary + rp.maxSalary) / 2) FROM RecruitmentPosition rp WHERE rp.isDeleted = false AND rp.minSalary IS NOT NULL AND rp.maxSalary IS NOT NULL";
            Query<Object[]> query = getCurrentSession().createQuery(hql, Object[].class);
            Object[] result = query.getSingleResult();

            Map<String, Object> salaryRange = new HashMap<>();
            salaryRange.put("min", result[0] != null ? result[0] : 5.0);
            salaryRange.put("max", result[1] != null ? result[1] : 100.0);
            salaryRange.put("average", result[2] != null ? result[2] : 25.0);

            return salaryRange;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> defaultRange = new HashMap<>();
            defaultRange.put("min", 5.0);
            defaultRange.put("max", 100.0);
            defaultRange.put("average", 25.0);
            return defaultRange;
        }
    }
    public long count() {
        Session session = sessionFactory.getCurrentSession();
        Query<Long> query = session.createQuery("SELECT COUNT(r) FROM RecruitmentPosition r", Long.class);
        return query.getSingleResult();
    }
}
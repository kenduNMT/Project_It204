package bk.dao.impl;

import bk.dao.CandidateDAO;
import bk.entity.Candidate;
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
public class CandidateDAOImpl implements CandidateDAO {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public Candidate save(Candidate candidate) {
        getCurrentSession().save(candidate);
        return candidate;
    }

    @Override
    public Candidate update(Candidate candidate) {
        getCurrentSession().update(candidate);
        return candidate;
    }

    @Override
    public Optional<Candidate> findById(Long id) {
        try {
            Candidate candidate = getCurrentSession().get(Candidate.class, id);
            if (candidate != null && !candidate.getIsDeleted()) {
                return Optional.of(candidate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Candidate> findByIdIncludeDeleted(Long id) {
        try {
            Candidate candidate = getCurrentSession().get(Candidate.class, id);
            return Optional.ofNullable(candidate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Candidate> findByEmail(String email) {
        try {
            String hql = "FROM Candidate c WHERE c.email = :email AND c.isDeleted = false";
            Query<Candidate> query = getCurrentSession().createQuery(hql, Candidate.class);
            query.setParameter("email", email);
            query.setMaxResults(1);

            List<Candidate> results = query.list();
            if (!results.isEmpty()) {
                return Optional.of(results.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            String hql = "SELECT COUNT(c) FROM Candidate c WHERE c.email = :email AND c.isDeleted = false";
            Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
            query.setParameter("email", email);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Candidate> findAllActive() {
        try {
            String hql = "FROM Candidate c WHERE c.isDeleted = false ORDER BY c.createdAt DESC";
            Query<Candidate> query = getCurrentSession().createQuery(hql, Candidate.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Candidate> findAllForExport() {
        try {
            String hql = "FROM Candidate c ORDER BY c.createdAt DESC";
            Query<Candidate> query = getCurrentSession().createQuery(hql, Candidate.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            String hql = "UPDATE Candidate c SET c.isDeleted = true WHERE c.id = :id";
            Query query = getCurrentSession().createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hardDeleteById(Long id) {
        try {
            String hql = "DELETE FROM Candidate c WHERE c.id = :id";
            Query query = getCurrentSession().createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long countActive() {
        try {
            String hql = "SELECT COUNT(c) FROM Candidate c WHERE c.isDeleted = false";
            Query<Long> query = getCurrentSession().createQuery(hql, Long.class);
            Long count = query.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Page<Candidate> findAll(Pageable pageable) {
        try {
            // Count query
            String countHql = "SELECT COUNT(c) FROM Candidate c";
            Query<Long> countQuery = getCurrentSession().createQuery(countHql, Long.class);
            Long totalElements = countQuery.uniqueResult();

            // Data query
            StringBuilder hql = new StringBuilder("FROM Candidate c ORDER BY ");
            if (pageable.getSort().isSorted()) {
                pageable.getSort().forEach(order -> {
                    hql.append("c.")
                            .append(order.getProperty())
                            .append(" ")
                            .append(order.getDirection().name())
                            .append(", ");
                });
                hql.setLength(hql.length() - 2); // Remove last comma
            } else {
                hql.append("c.createdAt DESC");
            }

            Query<Candidate> query = getCurrentSession().createQuery(hql.toString(), Candidate.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Candidate> content = query.list();
            return new PageImpl<>(content, pageable, totalElements != null ? totalElements : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    @Override
    public Page<Candidate> searchCandidates(String search, String experience, String gender, String technologyId, Pageable pageable) {
        try {
            StringBuilder whereClause = new StringBuilder("WHERE c.isDeleted = false ");

            if (search != null && !search.isEmpty()) {
                whereClause.append("AND (LOWER(c.name) LIKE LOWER(:search) ")
                        .append("OR LOWER(c.email) LIKE LOWER(:search) ")
                        .append("OR LOWER(c.phone) LIKE LOWER(:search)) ");
            }

            if (experience != null && !experience.isEmpty()) {
                whereClause.append("AND c.experience >= :experience ");
            }

            if (gender != null && !gender.isEmpty()) {
                whereClause.append("AND c.gender = :gender ");
            }

            if (technologyId != null && !technologyId.isEmpty()) {
                whereClause.append("AND EXISTS (SELECT 1 FROM c.technologies t WHERE t.id = :techId) ");
            }

            // Count query
            String countHql = "SELECT COUNT(c) FROM Candidate c " + whereClause;
            Query<Long> countQuery = getCurrentSession().createQuery(countHql, Long.class);

            // Data query
            String hql = "FROM Candidate c " + whereClause + " ORDER BY c.createdAt DESC";
            Query<Candidate> query = getCurrentSession().createQuery(hql, Candidate.class);

            // Set parameters
            if (search != null && !search.isEmpty()) {
                countQuery.setParameter("search", "%" + search + "%");
                query.setParameter("search", "%" + search + "%");
            }

            if (experience != null && !experience.isEmpty()) {
                countQuery.setParameter("experience", Integer.parseInt(experience));
                query.setParameter("experience", Integer.parseInt(experience));
            }

            if (gender != null && !gender.isEmpty()) {
                countQuery.setParameter("gender", gender);
                query.setParameter("gender", gender);
            }

            if (technologyId != null && !technologyId.isEmpty()) {
                countQuery.setParameter("techId", Integer.parseInt(technologyId));
                query.setParameter("techId", Integer.parseInt(technologyId));
            }

            Long totalElements = countQuery.uniqueResult();

            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Candidate> content = query.list();
            return new PageImpl<>(content, pageable, totalElements != null ? totalElements : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    @Override
    public long countByFilter(String search, String status, String gender) {
        try {
            StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");

            if (search != null && !search.trim().isEmpty()) {
                whereClause.append("AND (LOWER(c.name) LIKE LOWER(:search) ")
                        .append("OR LOWER(c.email) LIKE LOWER(:search) ")
                        .append("OR LOWER(c.phone) LIKE LOWER(:search)) ");
            }

            if (status != null && !status.isEmpty()) {
                whereClause.append("AND c.status = :status ");
            }

            if (gender != null && !gender.isEmpty()) {
                whereClause.append("AND c.gender = :gender ");
            }

            String countHql = "SELECT COUNT(c) FROM Candidate c " + whereClause;
            Query<Long> countQuery = getCurrentSession().createQuery(countHql, Long.class);

            setQueryParameters(countQuery, search, status, gender);
            Long count = countQuery.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void updateCandidateStatus(Long id, String status) {
        try {
            String hql = "UPDATE Candidate c SET c.status = :status WHERE c.id = :id";
            Query query = getCurrentSession().createQuery(hql);
            query.setParameter("status", status);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setQueryParameters(Query query, String search, String status, String gender) {
        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.trim() + "%");
        }

        if (status != null && !status.isEmpty()) {
            query.setParameter("status", status);
        }

        if (gender != null && !gender.isEmpty()) {
            query.setParameter("gender", gender);
        }
    }
}
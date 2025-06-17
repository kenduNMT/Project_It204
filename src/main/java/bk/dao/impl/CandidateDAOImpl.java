package bk.dao.impl;

import bk.dao.CandidateDAO;
import bk.entity.Candidate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
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
}
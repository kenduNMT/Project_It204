package bk.dao;

import bk.entity.Candidate;

import java.util.List;
import java.util.Optional;

public interface CandidateDAO {
    Candidate save(Candidate candidate);
    Candidate update(Candidate candidate);
    List<Candidate> findAllActive();
    Optional<Candidate> findById(Long id);
    Optional<Candidate> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    long countActive();
}

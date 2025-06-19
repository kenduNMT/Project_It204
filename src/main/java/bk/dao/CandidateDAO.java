package bk.dao;

import bk.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    /**
     * Lấy tất cả candidates với pagination
     */
    Page<Candidate> findAll(Pageable pageable);

    /**
     * Tìm kiếm candidates với filter và pagination
     */
    Page<Candidate> searchCandidates(String search, String experience, String gender, String technologyId, Pageable pageable);

    /**
     * Lấy candidate theo ID (bao gồm cả deleted)
     */
    Optional<Candidate> findByIdIncludeDeleted(Long id);

    /**
     * Lấy tất cả candidates (bao gồm cả deleted) để export
     */
    List<Candidate> findAllForExport();

    /**
     * Đếm tổng số candidates với filter
     */
    long countByFilter(String search, String status, String gender);

    /**
     * Cập nhật status của candidate
     */
    void updateCandidateStatus(Long id, String status);

    /**
     * Hard delete candidate (nếu cần)
     */
    void hardDeleteById(Long id);
}

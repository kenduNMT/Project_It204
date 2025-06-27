package bk.dao;

import bk.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CandidateDAO {

    Candidate save(Candidate candidate);
    Candidate update(Candidate candidate);
    Optional<Candidate> findById(Long id);
    Optional<Candidate> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    long countActive();

    // ===== Pagination and Search =====
    /**
     * Lấy tất cả candidates với pagination
     */
    Page<Candidate> findAll(Pageable pageable);

    /**
     * Tìm kiếm candidates với filter và pagination
     */
    Page<Candidate> searchCandidates(String search, String experience, String gender, String technology, Pageable pageable);

    /**
     * Lấy candidate theo ID (bao gồm cả deleted)
     */
    Optional<Candidate> findByIdIncludeDeleted(Long id);

    /**
     * Cập nhật mật khẩu của ứng viên
     */
    void updatePassword(Long candidateId, String encodedPassword);

    /**
     * Legacy search method - sử dụng Spring Pageable
     */
    List<Candidate> searchCandidatesLegacy(String search, String experience, String gender, String technologyId, Pageable pageable);

    long count();
}
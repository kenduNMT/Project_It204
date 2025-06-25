package bk.dao;

import bk.entity.Candidate;
import bk.entity.Technology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CandidateDAO {

    // ===== Basic CRUD Operations =====
    Candidate save(Candidate candidate);
    Candidate update(Candidate candidate);
    List<Candidate> findAllActive();
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

    // ===== Technology Management =====

    /**
     * Lấy danh sách công nghệ của ứng viên
     */
    List<Technology> getCandidateTechnologies(Long candidateId);

    /**
     * Thêm công nghệ cho ứng viên
     */
    void addTechnologyToCandidate(Long candidateId, Integer technologyId);

    /**
     * Xóa công nghệ khỏi ứng viên
     */
    void removeTechnologyFromCandidate(Long candidateId, Integer technologyId);

    /**
     * Kiểm tra ứng viên có công nghệ này không
     */
    boolean candidateHasTechnology(Long candidateId, Integer technologyId);

    /**
     * Xóa tất cả công nghệ của ứng viên
     */
    void removeAllTechnologiesFromCandidate(Long candidateId);

    /**
     * Cập nhật mật khẩu của ứng viên
     */
    void updatePassword(Long candidateId, String encodedPassword);

    // ===== Legacy Methods (để tương thích với CandidateService) =====
    /**
     * Legacy search method - sử dụng Spring Pageable
     */
    List<Candidate> searchCandidatesLegacy(String search, String experience, String gender, String technologyId, Pageable pageable);

    /**
     * Đếm số lượng kết quả tìm kiếm
     */
    long countSearchResults(String search, String status, String gender);

    /**
     * Lấy danh sách candidates với filter và pagination (legacy)
     */
    List<Candidate> findAllWithFilter(String status, String gender, Pageable pageable);

    /**
     * Đếm số lượng candidates với filter (legacy)
     */
    long countWithFilter(String status, String gender);

    /**
     * Lấy danh sách candidates theo page (legacy)
     */
    List<Candidate> findAllWithPagination(Pageable pageable);
}
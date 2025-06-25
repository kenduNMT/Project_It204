package bk.service;

import bk.dto.CandidateLoginDTO;
import bk.dto.CandidateRegistrationDTO;
import bk.entity.Candidate;
import bk.entity.Technology;
import bk.exception.EmailAlreadyExistsException;
import bk.exception.InvalidCredentialsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Candidate
 */
public interface CandidateService {

    /**
     * Đăng ký ứng viên mới
     */
    Candidate register(CandidateRegistrationDTO registrationDTO) throws EmailAlreadyExistsException;

    /**
     * Đăng nhập ứng viên
     */
    Candidate login(CandidateLoginDTO loginDTO) throws InvalidCredentialsException;

    /**
     * Tìm ứng viên theo ID
     */
    Optional<Candidate> findById(Long id);

    /**
     * Tìm ứng viên theo email
     */
    Optional<Candidate> findByEmail(String email);

    /**
     * Cập nhật thông tin profile ứng viên
     */
    Candidate updateProfile(Candidate candidate);

    /**
     * Lấy danh sách tất cả ứng viên đang hoạt động
     */
    List<Candidate> findAllActive();

    /**
     * Xóa ứng viên (soft delete)
     */
    void deleteById(Long id);

    /**
     * Đếm số lượng ứng viên đang hoạt động
     */
    long countActive();

    /**
     * Kiểm tra mật khẩu
     */
    boolean checkPassword(String rawPassword, String encodedPassword);

    /**
     * Kiểm tra mật khẩu theo ID ứng viên
     */
    boolean checkPassword(Long candidateId, String rawPassword);

    /**
     * Cập nhật mật khẩu
     */
    void updatePassword(Long candidateId, String newPassword);

    /**
     * Mã hóa mật khẩu
     */
    String encodePassword(String rawPassword);

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);

    // ===== Methods for Management Controller =====

    /**
     * Lấy tất cả candidates với pagination
     */
    Page<Candidate> getAllCandidates(Pageable pageable);

    /**
     * Tìm kiếm candidates với filter và pagination
     */
    Page<Candidate> searchCandidates(String search, String experience, String gender, String technology, Pageable pageable);

    /**
     * Lấy candidate theo ID (cho admin)
     */
    Candidate getCandidateById(Long id);

    /**
     * Cập nhật candidate (cho admin)
     */
    Candidate updateCandidate(Candidate candidate);

    /**
     * Xóa candidate (cho admin)
     */
    void deleteCandidate(Long id);

    // ===== Technology Management Methods =====

    /**
     * Lấy danh sách công nghệ của ứng viên
     */
    List<Technology> getCandidateTechnologies(Long candidateId);

    /**
     * Thêm công nghệ cho ứng viên
     */
    void addTechnology(Long candidateId, Integer technologyId);

    /**
     * Xóa công nghệ khỏi ứng viên
     */
    void removeTechnology(Long candidateId, Integer technologyId);

    /**
     * Kiểm tra ứng viên có công nghệ này không
     */
    boolean hasTechnology(Long candidateId, Integer technologyId);

    /**
     * Cập nhật danh sách công nghệ của ứng viên
     */
    void updateCandidateTechnologies(Long candidateId, List<Integer> technologyIds);

    /**
     * Thêm công nghệ cho ứng viên (trả về boolean)
     */
    boolean addTechnologyToCandidate(Long candidateId, Integer techId);

    /**
     * Xóa công nghệ khỏi ứng viên (trả về boolean)
     */
    boolean removeTechnologyFromCandidate(Long candidateId, Integer techId);

    // ===== Legacy methods (deprecated) =====
    /**
     * @deprecated Use Page<Candidate> searchCandidates instead
     */
    @Deprecated
    List<Candidate> searchCandidatesLegacy(String search, String experience, String gender, String technologyId, Pageable pageable);

    /**
     * Đếm số lượng kết quả tìm kiếm
     */
    long countSearchResults(String search, String status, String gender);

    /**
     * Lấy danh sách candidates với filter và pagination
     */
    List<Candidate> findAllWithFilter(String status, String gender, Pageable pageable);

    /**
     * Đếm số lượng candidates với filter
     */
    long countWithFilter(String status, String gender);

    /**
     * Lấy danh sách candidates theo page
     */
    List<Candidate> findAllWithPagination(Pageable pageable);
}
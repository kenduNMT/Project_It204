package bk.service;

import bk.dto.CandidateLoginDTO;
import bk.dto.CandidateRegistrationDTO;
import bk.entity.Candidate;
import bk.exception.EmailAlreadyExistsException;
import bk.exception.InvalidCredentialsException;

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
     * Mã hóa mật khẩu
     */
    String encodePassword(String rawPassword);

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);
}
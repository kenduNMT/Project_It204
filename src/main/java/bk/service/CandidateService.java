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

public interface CandidateService {
    Candidate register(CandidateRegistrationDTO registrationDTO) throws EmailAlreadyExistsException;

    Candidate login(CandidateLoginDTO loginDTO) throws InvalidCredentialsException;

    Optional<Candidate> findById(Long id);

    Optional<Candidate> findByEmail(String email);

    Candidate updateProfile(Candidate candidate);

    long countActive();

    boolean checkPassword(String rawPassword, String encodedPassword);

    boolean checkPassword(Long candidateId, String rawPassword);

    void updatePassword(Long candidateId, String newPassword);

    String encodePassword(String rawPassword);

    Page<Candidate> searchCandidates(String search, String experience, String gender, String technology, Pageable pageable);

    Candidate getCandidateById(Long id);

    void updateCandidate(Candidate candidate);

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

    long getTotalCount();
}
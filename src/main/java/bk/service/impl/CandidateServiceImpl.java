package bk.service.impl;

import bk.dao.CandidateDAO;
import bk.dto.CandidateLoginDTO;
import bk.dto.CandidateRegistrationDTO;
import bk.entity.Candidate;
import bk.entity.Technology;
import bk.exception.EmailAlreadyExistsException;
import bk.exception.InvalidCredentialsException;
import bk.service.CandidateService;
import bk.service.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CandidateServiceImpl implements CandidateService {
    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private CandidateDAO candidateDAO;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Candidate register(CandidateRegistrationDTO registrationDTO) throws EmailAlreadyExistsException {
        // Kiểm tra email đã tồn tại chưa
        if (candidateDAO.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng: " + registrationDTO.getEmail());
        }

        // Tạo candidate mới
        Candidate candidate = new Candidate();
        candidate.setName(registrationDTO.getName());
        candidate.setEmail(registrationDTO.getEmail());
        candidate.setPassword(encodePassword(registrationDTO.getPassword()));
        candidate.setPhone(registrationDTO.getPhone());
        candidate.setGender(registrationDTO.getGender());
        candidate.setDob(registrationDTO.getDob());
        candidate.setDescription(registrationDTO.getDescription());
        candidate.setExperience(registrationDTO.getExperience());
        candidate.setStatus(Candidate.Status.valueOf("ACTIVE"));
        candidate.setIsDeleted(false);

        return candidateDAO.save(candidate);
    }

    @Override
    public Candidate login(CandidateLoginDTO loginDTO) throws InvalidCredentialsException {
        // Tìm candidate theo email
        Optional<Candidate> candidateOpt = candidateDAO.findByEmail(loginDTO.getEmail());

        if (!candidateOpt.isPresent()) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không chính xác");
        }

        Candidate candidate = candidateOpt.get();

        // Kiểm tra mật khẩu
        if (!checkPassword(loginDTO.getPassword(), candidate.getPassword())) {
            throw new InvalidCredentialsException("Email hoặc mật khẩu không chính xác");
        }

        // Kiểm tra trạng thái tài khoản
        if (candidate.getIsDeleted()) {
            throw new InvalidCredentialsException("Tài khoản đã bị vô hiệu hóa");
        }

        return candidate;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Candidate> findById(Long id) {
        return candidateDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Candidate> findByEmail(String email) {
        return candidateDAO.findByEmail(email);
    }

    @Override
    public Candidate updateProfile(Candidate candidate) {
        // Cập nhật thời gian sửa đổi
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateDAO.update(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidate> findAllActive() {
        return candidateDAO.findAllActive();
    }

    @Override
    public void deleteById(Long id) {
        candidateDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        return candidateDAO.countActive();
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return candidateDAO.existsByEmail(email);
    }

    // ===== Management Controller Methods =====

    @Override
    @Transactional(readOnly = true)
    public Page<Candidate> getAllCandidates(Pageable pageable) {
        return candidateDAO.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidates(String search, String experience, String gender, String technologyId, Pageable pageable) {
        return candidateDAO.searchCandidates(search, experience, gender, technologyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Candidate getCandidateById(Long id) {
        Optional<Candidate> candidate = candidateDAO.findByIdIncludeDeleted(id);
        return candidate.orElse(null);
    }

    @Override
    public Candidate updateCandidate(Candidate candidate) {
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateDAO.update(candidate);
    }

    @Override
    public void deleteCandidate(Long id) {
        candidateDAO.deleteById(id);
    }

    @Override
    @Transactional
    public boolean addTechnologyToCandidate(Long candidateId, Integer techId) {
        Candidate candidate = candidateDAO.findById(candidateId).orElse(null);
        Technology technology = technologyService.findById(techId).orElse(null);

        if (candidate != null && technology != null) {
            candidate.getTechnologies().add(technology);
            candidateDAO.save(candidate);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean removeTechnologyFromCandidate(Long candidateId, Integer techId) {
        Candidate candidate = candidateDAO.findById(candidateId).orElse(null);
        Technology technology = technologyService.findById(techId).orElse(null);

        if (candidate != null && technology != null) {
            candidate.getTechnologies().remove(technology);
            candidateDAO.save(candidate);
            return true;
        }
        return false;
    }


    // ===== Legacy methods (keep for backward compatibility) =====

    @Override
    @Deprecated
    public List<Candidate> searchCandidates(String search, String experience, String gender, String technologyId, java.awt.print.Pageable pageable) {
        return Collections.emptyList();
    }

    @Override
    public long countSearchResults(String search, String status, String gender) {
        return candidateDAO.countByFilter(search, status, gender);
    }

    @Override
    public List<Candidate> findAllWithFilter(String status, String gender, java.awt.print.Pageable pageable) {
        return Collections.emptyList();
    }

    @Override
    public long countWithFilter(String status, String gender) {
        return candidateDAO.countByFilter(null, status, gender);
    }

    @Override
    public List<Candidate> findAllWithPagination(java.awt.print.Pageable pageable) {
        return Collections.emptyList();
    }
}
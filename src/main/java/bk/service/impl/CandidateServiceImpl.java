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
import java.util.ArrayList;
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
    public long countActive() {
        return candidateDAO.countActive();
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean checkPassword(Long candidateId, String rawPassword) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);
        return candidateOpt.filter(candidate -> checkPassword(rawPassword, candidate.getPassword())).isPresent();
    }

    @Override
    public void updatePassword(Long candidateId, String newPassword) {
        String encodedPassword = encodePassword(newPassword);
        candidateDAO.updatePassword(candidateId, encodedPassword);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // ===== Management Controller Methods =====

    @Override
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidates(String search, String experience, String gender, String technology, Pageable pageable) {
        return candidateDAO.searchCandidates(search, experience, gender, technology, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Candidate getCandidateById(Long id) {
        Optional<Candidate> candidate = candidateDAO.findByIdIncludeDeleted(id);
        return candidate.orElse(null);
    }

    @Override
    public void updateCandidate(Candidate candidate) {
        candidate.setUpdatedAt(LocalDateTime.now());
        candidateDAO.update(candidate);
    }

    @Override
    public void deleteCandidate(Long id) {
        candidateDAO.deleteById(id);
    }

    // ===== Technology Management Methods =====

    @Override
    @Transactional(readOnly = true)
    public List<Technology> getCandidateTechnologies(Long candidateId) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);
        if (candidateOpt.isPresent()) {
            return new ArrayList<>(candidateOpt.get().getTechnologies());
        }
        return Collections.emptyList();
    }

    @Override
    public void addTechnology(Long candidateId, Integer technologyId) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);
        Optional<Technology> technologyOpt = technologyService.findById(technologyId);

        if (candidateOpt.isPresent() && technologyOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            Technology technology = technologyOpt.get();

            if (!candidate.getTechnologies().contains(technology)) {
                candidate.getTechnologies().add(technology);
                candidateDAO.update(candidate);
            }
        }
    }

    @Override
    public void removeTechnology(Long candidateId, Integer technologyId) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);
        Optional<Technology> technologyOpt = technologyService.findById(technologyId);

        if (candidateOpt.isPresent() && technologyOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();
            Technology technology = technologyOpt.get();

            candidate.getTechnologies().remove(technology);
            candidateDAO.update(candidate);
        }
    }

    @Override
    public boolean addTechnologyToCandidate(Long candidateId, Integer techId) {
        try {
            addTechnology(candidateId, techId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeTechnologyFromCandidate(Long candidateId, Integer techId) {
        try {
            removeTechnology(candidateId, techId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== Legacy methods (deprecated) =====

    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public List<Candidate> searchCandidatesLegacy(String search, String experience, String gender, String technologyId, Pageable pageable) {
        return candidateDAO.searchCandidatesLegacy(search, experience, gender, technologyId, pageable);
    }

    @Override
    public long getTotalCount() {
        return candidateDAO.count();
    }
}
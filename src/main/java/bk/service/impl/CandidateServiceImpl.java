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
    public boolean checkPassword(Long candidateId, String rawPassword) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);
        if (candidateOpt.isPresent()) {
            return checkPassword(rawPassword, candidateOpt.get().getPassword());
        }
        return false;
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
    public Candidate updateCandidate(Candidate candidate) {
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateDAO.update(candidate);
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
    @Transactional(readOnly = true)
    public boolean hasTechnology(Long candidateId, Integer technologyId) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);
        Optional<Technology> technologyOpt = technologyService.findById(technologyId);

        if (candidateOpt.isPresent() && technologyOpt.isPresent()) {
            return candidateOpt.get().getTechnologies().contains(technologyOpt.get());
        }
        return false;
    }

    @Override
    public void updateCandidateTechnologies(Long candidateId, List<Integer> technologyIds) {
        Optional<Candidate> candidateOpt = candidateDAO.findById(candidateId);

        if (candidateOpt.isPresent()) {
            Candidate candidate = candidateOpt.get();

            // Xóa tất cả technologies hiện tại
            candidate.getTechnologies().clear();

            // Thêm technologies mới
            for (Integer techId : technologyIds) {
                Optional<Technology> technologyOpt = technologyService.findById(techId);
                if (technologyOpt.isPresent()) {
                    candidate.getTechnologies().add(technologyOpt.get());
                }
            }

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
    @Transactional(readOnly = true)
    public long countSearchResults(String search, String status, String gender) {
        return candidateDAO.countSearchResults(search, status, gender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidate> findAllWithFilter(String status, String gender, Pageable pageable) {
        return candidateDAO.findAllWithFilter(status, gender, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countWithFilter(String status, String gender) {
        return candidateDAO.countWithFilter(status, gender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidate> findAllWithPagination(Pageable pageable) {
        return candidateDAO.findAllWithPagination(pageable);
    }
}
package bk.service.impl;

import bk.dao.CandidateDAO;
import bk.dto.CandidateLoginDTO;
import bk.dto.CandidateRegistrationDTO;
import bk.entity.Candidate;
import bk.exception.EmailAlreadyExistsException;
import bk.exception.InvalidCredentialsException;
import bk.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CandidateServiceImpl implements CandidateService {

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
        candidate.setStatus("ACTIVE");
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
}
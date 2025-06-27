package bk.service.impl;

import bk.dao.RecruitmentPositionDAO;
import bk.dao.TechnologyDAO;
import bk.entity.RecruitmentPosition;
import bk.entity.Technology;
import bk.service.RecruitmentPositionService;
import bk.utils.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecruitmentPositionServiceImpl implements RecruitmentPositionService {

    private final RecruitmentPositionDAO recruitmentPositionDAO;
    private final TechnologyDAO technologyDAO;

    @Autowired
    public RecruitmentPositionServiceImpl(RecruitmentPositionDAO recruitmentPositionDAO,
                                          TechnologyDAO technologyDAO) {
        this.recruitmentPositionDAO = recruitmentPositionDAO;
        this.technologyDAO = technologyDAO;
    }

    @Override
    public List<RecruitmentPosition> findAll() {
        return recruitmentPositionDAO.findAll();
    }

    @Override
    public PageResponse<RecruitmentPosition> findAll(int page, int size) {
        List<RecruitmentPosition> positions = recruitmentPositionDAO.findAll(page, size);
        long totalElements = recruitmentPositionDAO.countAll();
        return new PageResponse<>(positions, page, size, totalElements);
    }

    @Override
    public PageResponse<RecruitmentPosition> findByName(String name, int page, int size) {
        List<RecruitmentPosition> positions = recruitmentPositionDAO.findByName(name, page, size);
        long totalElements = recruitmentPositionDAO.countByName(name);
        return new PageResponse<>(positions, page, size, totalElements);
    }

    @Override
    public PageResponse<RecruitmentPosition> findActivePositions(int page, int size) {
        List<RecruitmentPosition> positions = recruitmentPositionDAO.findActivePositions(page, size);
        long totalElements = recruitmentPositionDAO.countActive();
        return new PageResponse<>(positions, page, size, totalElements);
    }

    @Override
    public PageResponse<RecruitmentPosition> findExpiredPositions(int page, int size) {
        List<RecruitmentPosition> positions = recruitmentPositionDAO.findExpiredPositions(page, size);
        long totalElements = recruitmentPositionDAO.countAll() - recruitmentPositionDAO.countActive();
        return new PageResponse<>(positions, page, size, totalElements);
    }

    @Override
    public RecruitmentPosition findById(Integer id) {
        return recruitmentPositionDAO.findById(id);
    }

    @Override
    public void save(RecruitmentPosition recruitmentPosition, List<Integer> technologyIds) {
        if (recruitmentPosition.getId() == null) {
            recruitmentPosition.setCreatedDate(LocalDate.now());
        }

        recruitmentPosition.clearTechnologies();

        if (technologyIds != null && !technologyIds.isEmpty()) {
            for (Integer techId : technologyIds) {
                Optional<Technology> technologyOptional = technologyDAO.findByIdAndActive(techId);
                if (technologyOptional.isPresent() && technologyOptional.get().isActive()) {
                    recruitmentPosition.addTechnology(technologyOptional.get());
                }
            }
        }

        recruitmentPositionDAO.save(recruitmentPosition);
    }

    @Override
    public void update(RecruitmentPosition recruitmentPosition, List<Integer> technologyIds) {
        RecruitmentPosition existingPosition = recruitmentPositionDAO.findById(recruitmentPosition.getId());
        if (existingPosition != null) {
            existingPosition.setName(recruitmentPosition.getName());
            existingPosition.setDescription(recruitmentPosition.getDescription());
            existingPosition.setMinSalary(recruitmentPosition.getMinSalary());
            existingPosition.setMaxSalary(recruitmentPosition.getMaxSalary());
            existingPosition.setMinExperience(recruitmentPosition.getMinExperience());
            existingPosition.setExpiredDate(recruitmentPosition.getExpiredDate());

            existingPosition.clearTechnologies();
            if (technologyIds != null && !technologyIds.isEmpty()) {
                for (Integer techId : technologyIds) {
                    Optional<Technology> technologyOptional = technologyDAO.findByIdAndActive(techId);
                    if (technologyOptional.isPresent() && technologyOptional.get().isActive()) {
                        existingPosition.addTechnology(technologyOptional.get());
                    }
                }
            }

            recruitmentPositionDAO.update(existingPosition);
        }
    }

    @Override
    public void delete(Integer id) {
        recruitmentPositionDAO.delete(id);
    }

    @Override
    public Long countAll() {
        return recruitmentPositionDAO.countAll();
    }

    @Override
    public Long countActive() {
        return recruitmentPositionDAO.countActive();
    }

    @Override
    public boolean isNameExists(String name, Integer excludeId) {
        List<RecruitmentPosition> positions = recruitmentPositionDAO.findByName(name);
        return positions.stream()
                .anyMatch(pos -> pos.getName().equalsIgnoreCase(name) &&
                        !pos.getId().equals(excludeId));
    }

    @Override
    public List<Technology> getAllActiveTechnologies() {
        return technologyDAO.findAll().stream()
                .filter(Technology::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<RecruitmentPosition> searchAndFilter(String keyword, String location, String category,
                                                           Double minSalary, Double maxSalary, Integer minExperience,
                                                           String sortBy, int page, int size) {
        try {
            List<RecruitmentPosition> positions = recruitmentPositionDAO.searchAndFilter(
                    keyword, location, category, minSalary, maxSalary, minExperience, sortBy, page, size);
            long totalElements = recruitmentPositionDAO.countSearchAndFilter(
                    keyword, location, category, minSalary, maxSalary, minExperience);
            return new PageResponse<>(positions, page, size, totalElements);
        } catch (Exception e) {
            e.printStackTrace();
            return new PageResponse<>();
        }
    }

    @Override
    public Map<String, Object> getSalaryRange() {
        try {
            return recruitmentPositionDAO.getSalaryRange();
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> defaultRange = new HashMap<>();
            defaultRange.put("min", 5.0);
            defaultRange.put("max", 100.0);
            defaultRange.put("average", 25.0);
            return defaultRange;
        }
    }

    @Override
    public long getTotalCount() {
        return recruitmentPositionDAO.count();
    }
}
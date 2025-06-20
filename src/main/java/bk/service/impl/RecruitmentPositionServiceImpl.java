package bk.service.impl;

import bk.dao.RecruitmentPositionDAO;
import bk.dao.TechnologyDAO;
import bk.entity.RecruitmentPosition;
import bk.entity.Technology;
import bk.service.RecruitmentPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    public void hardDelete(Integer id) {
        recruitmentPositionDAO.hardDelete(id);
    }

    @Override
    public List<RecruitmentPosition> findByName(String name) {
        return recruitmentPositionDAO.findByName(name);
    }

    @Override
    public List<RecruitmentPosition> findActivePositions() {
        return recruitmentPositionDAO.findActivePositions();
    }

    @Override
    public List<RecruitmentPosition> findExpiredPositions() {
        return recruitmentPositionDAO.findExpiredPositions();
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
}
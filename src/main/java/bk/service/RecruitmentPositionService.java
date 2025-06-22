package bk.service;

import bk.entity.RecruitmentPosition;
import bk.entity.Technology;
import bk.utils.PageResponse;

import java.util.List;

public interface RecruitmentPositionService {

    List<RecruitmentPosition> findAll();
    PageResponse<RecruitmentPosition> findAll(int page, int size);
    PageResponse<RecruitmentPosition> findByName(String name, int page, int size);
    PageResponse<RecruitmentPosition> findActivePositions(int page, int size);
    PageResponse<RecruitmentPosition> findExpiredPositions(int page, int size);

    RecruitmentPosition findById(Integer id);
    void save(RecruitmentPosition recruitmentPosition, List<Integer> technologyIds);
    void update(RecruitmentPosition recruitmentPosition, List<Integer> technologyIds);
    void delete(Integer id);
    void hardDelete(Integer id);

    List<RecruitmentPosition> findByName(String name);
    List<RecruitmentPosition> findActivePositions();
    List<RecruitmentPosition> findExpiredPositions();

    Long countAll();
    Long countActive();

    boolean isNameExists(String name, Integer excludeId);
    List<Technology> getAllActiveTechnologies();
}
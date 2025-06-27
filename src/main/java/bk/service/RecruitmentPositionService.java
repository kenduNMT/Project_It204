package bk.service;

import bk.entity.RecruitmentPosition;
import bk.entity.Technology;
import bk.utils.PageResponse;

import java.util.List;
import java.util.Map;

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

    Long countAll();
    Long countActive();

    boolean isNameExists(String name, Integer excludeId);
    List<Technology> getAllActiveTechnologies();
    
    // Advanced search and filter methods
    PageResponse<RecruitmentPosition> searchAndFilter(String keyword, String location, String category,
                                                     Double minSalary, Double maxSalary, Integer minExperience,
                                                     String sortBy, int page, int size);

    Map<String, Object> getSalaryRange();

    long getTotalCount();
}
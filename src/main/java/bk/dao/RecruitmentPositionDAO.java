package bk.dao;

import bk.entity.RecruitmentPosition;
import java.util.List;
import java.util.Map;

public interface RecruitmentPositionDAO {
    List<RecruitmentPosition> findAll();
    List<RecruitmentPosition> findAll(int page, int size);
    List<RecruitmentPosition> findByName(String name, int page, int size);
    List<RecruitmentPosition> findActivePositions(int page, int size);
    List<RecruitmentPosition> findExpiredPositions(int page, int size);

    RecruitmentPosition findById(Integer id);
    void save(RecruitmentPosition recruitmentPosition);
    void update(RecruitmentPosition recruitmentPosition);
    void delete(Integer id);

    List<RecruitmentPosition> findByName(String name);

    Long countAll();
    Long countActive();
    Long countByName(String name);
    
    // Advanced search and filter methods
    List<RecruitmentPosition> searchAndFilter(String keyword, String location, String category,
                                             Double minSalary, Double maxSalary, Integer minExperience,
                                             String sortBy, int page, int size);
    Long countSearchAndFilter(String keyword, String location, String category,
                             Double minSalary, Double maxSalary, Integer minExperience);

    Map<String, Object> getSalaryRange();

    long count();
}
package bk.dao;

import bk.entity.RecruitmentPosition;
import java.util.List;

public interface RecruitmentPositionDAO {
    List<RecruitmentPosition> findAll();
    RecruitmentPosition findById(Integer id);
    void save(RecruitmentPosition recruitmentPosition);
    void update(RecruitmentPosition recruitmentPosition);
    void delete(Integer id);
    void hardDelete(Integer id);
    List<RecruitmentPosition> findByName(String name);
    List<RecruitmentPosition> findActivePositions();
    List<RecruitmentPosition> findExpiredPositions();
    Long countAll();
    Long countActive();
}
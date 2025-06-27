package bk.service.impl;

import bk.dao.TechnologyDAO; // Đã đổi từ TechnologyDaoImpl sang TechnologyDAO (interface)
import bk.entity.Technology;
import bk.service.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TechnologyServiceImpl implements TechnologyService {

    @Autowired
    private TechnologyDAO technologyDao;

    @Override
    public List<Technology> getAllActiveTechnologies() {
        return technologyDao.findAllActive();
    }

    @Override
    public Page<Technology> getAllActiveTechnologies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return technologyDao.findAllActive(pageable);
    }

    @Override
    public Optional<Technology> findById(Integer id) {
        // Tìm theo ID và chỉ trả về nếu đang hoạt động
        return technologyDao.findByIdAndActive(id);
    }

    @Override
    public void createTechnology(Technology technology) {
        if (technologyDao.existsByNameAndActive(technology.getName())) {
            throw new RuntimeException("Tên công nghệ đã tồn tại: " + technology.getName());
        }
        technology.setIsDeleted(false); // Đảm bảo khi tạo mới là chưa xóa
        technologyDao.save(technology);
    }

    @Override
    public void updateTechnology(Integer id, Technology updatedTechnology) {
        // Tìm công nghệ hiện có, chỉ tìm những cái chưa bị xóa mềm
        Technology existingTechnology = technologyDao.findByIdAndActive(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công nghệ với ID: " + id));

        // Kiểm tra tên có trùng với công nghệ khác không (trừ chính nó)
        if (technologyDao.existsByNameAndNotId(updatedTechnology.getName(), id)) {
            throw new RuntimeException("Tên công nghệ đã tồn tại: " + updatedTechnology.getName());
        }

        // Cập nhật các trường cần thiết
        existingTechnology.setName(updatedTechnology.getName());
        // Thêm các trường khác nếu có
        // existingTechnology.setDescription(updatedTechnology.getDescription());
        // ...

        technologyDao.update(existingTechnology);
    }

    @Override
    public void softDeleteTechnology(Integer id) {
        // Kiểm tra xem công nghệ có tồn tại và đang hoạt động không
        technologyDao.findByIdAndActive(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công nghệ đang hoạt động với ID: " + id));
        // Thực hiện xóa mềm thông qua DAO
        technologyDao.softDeleteById(id);
    }

    @Override
    public void restoreTechnology(Integer id) {
        // Tìm công nghệ (bao gồm cả đã xóa mềm)
        technologyDao.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công nghệ với ID: " + id));
        // Thực hiện khôi phục thông qua DAO
        technologyDao.restoreById(id);
    }

    @Override
    public void permanentDeleteTechnology(Integer id) {
        // Kiểm tra xem công nghệ có tồn tại (bao gồm cả đã xóa mềm)
        technologyDao.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công nghệ với ID: " + id));
        // Thực hiện xóa vĩnh viễn thông qua DAO
        technologyDao.hardDeleteById(id);
    }

    @Override
    public Page<Technology> searchTechnologies(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return technologyDao.searchByName(keyword, pageable);
    }

    @Override
    public Page<Technology> getDeletedTechnologies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return technologyDao.findAllDeleted(pageable);
    }

    @Override
    public boolean isNameExists(String name) {
        return technologyDao.existsByNameAndActive(name);
    }

    @Override
    public boolean isNameExistsExceptId(String name, Integer id) {
        return technologyDao.existsByNameAndNotId(name, id);
    }

    @Override
    public List<Technology> findAllActive() {
        return technologyDao.findAllActive();
    }

    @Override
    public long getTotalCount() {
        return technologyDao.count();
    }

}
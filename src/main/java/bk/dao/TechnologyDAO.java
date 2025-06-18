package bk.dao;

import bk.entity.Technology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TechnologyDAO {

    /**
     * Lưu một công nghệ mới vào cơ sở dữ liệu.
     * @param technology Đối tượng Technology cần lưu.
     * @return Đối tượng Technology đã được lưu.
     */
    Technology save(Technology technology);

    /**
     * Cập nhật thông tin của một công nghệ hiện có.
     * @param technology Đối tượng Technology cần cập nhật.
     * @return Đối tượng Technology đã được cập nhật.
     */
    Technology update(Technology technology);

    /**
     * Tìm tất cả các công nghệ đang hoạt động (chưa bị xóa mềm).
     * Sắp xếp theo tên tăng dần.
     * @return Danh sách các đối tượng Technology đang hoạt động.
     */
    List<Technology> findAllActive();

    /**
     * Tìm tất cả các công nghệ đang hoạt động với phân trang.
     * Sắp xếp theo tên tăng dần.
     * @param pageable Đối tượng Pageable cho thông tin phân trang.
     * @return Trang chứa các đối tượng Technology đang hoạt động.
     */
    Page<Technology> findAllActive(Pageable pageable);

    /**
     * Tìm công nghệ theo ID, chỉ trả về nếu công nghệ đang hoạt động (chưa bị xóa mềm).
     * @param id ID của công nghệ.
     * @return Optional chứa đối tượng Technology nếu tìm thấy và đang hoạt động, ngược lại là Optional.empty().
     */
    Optional<Technology> findByIdAndActive(Integer id);

    /**
     * Tìm công nghệ theo tên, chỉ trả về nếu công nghệ đang hoạt động.
     * @param name Tên của công nghệ.
     * @return Optional chứa đối tượng Technology nếu tìm thấy và đang hoạt động, ngược lại là Optional.empty().
     */
    Optional<Technology> findByNameAndActive(String name);

    /**
     * Kiểm tra xem một tên công nghệ đã tồn tại cho các công nghệ đang hoạt động khác với ID cho trước hay không.
     * @param name Tên công nghệ cần kiểm tra.
     * @param id ID của công nghệ hiện tại (để loại trừ chính nó).
     * @return true nếu tên tồn tại cho một công nghệ khác đang hoạt động, ngược lại là false.
     */
    boolean existsByNameAndNotId(String name, Integer id);

    /**
     * Kiểm tra xem một tên công nghệ đã tồn tại trong các công nghệ đang hoạt động hay không.
     * @param name Tên công nghệ cần kiểm tra.
     * @return true nếu tên tồn tại trong các công nghệ đang hoạt động, ngược lại là false.
     */
    boolean existsByNameAndActive(String name);

    /**
     * Tìm kiếm công nghệ theo từ khóa trong tên, chỉ các công nghệ đang hoạt động.
     * @param keyword Từ khóa để tìm kiếm trong tên công nghệ.
     * @param pageable Đối tượng Pageable cho thông tin phân trang.
     * @return Trang chứa các đối tượng Technology phù hợp.
     */
    Page<Technology> searchByName(String keyword, Pageable pageable);

    /**
     * Đếm tổng số công nghệ đang hoạt động (chưa bị xóa mềm).
     * @return Số lượng công nghệ đang hoạt động.
     */
    long countActive();

    /**
     * Đếm tổng số công nghệ đã bị xóa mềm.
     * @return Số lượng công nghệ đã bị xóa.
     */
    long countDeleted();

    /**
     * Tìm tất cả các công nghệ đã bị xóa mềm với phân trang.
     * Sắp xếp theo tên tăng dần.
     * @param pageable Đối tượng Pageable cho thông tin phân trang.
     * @return Trang chứa các đối tượng Technology đã bị xóa.
     */
    Page<Technology> findAllDeleted(Pageable pageable);

    /**
     * Khôi phục một công nghệ đã bị xóa mềm (đặt isDeleted về false).
     * @param id ID của công nghệ cần khôi phục.
     */
    void restoreById(Integer id);

    /**
     * Xóa mềm một công nghệ (đặt isDeleted về true).
     * @param id ID của công nghệ cần xóa mềm.
     */
    void softDeleteById(Integer id);

    /**
     * Xóa vĩnh viễn một công nghệ khỏi cơ sở dữ liệu.
     * @param id ID của công nghệ cần xóa vĩnh viễn.
     */
    void hardDeleteById(Integer id);

    /**
     * Lấy công nghệ theo ID (bao gồm cả đã xóa mềm).
     * @param id ID của công nghệ.
     * @return Optional chứa đối tượng Technology nếu tìm thấy, ngược lại là Optional.empty().
     */
    Optional<Technology> findByIdIncludeDeleted(Integer id);

    /**
     * Tìm tất cả các công nghệ (bao gồm cả đã xóa mềm).
     * @return Danh sách tất cả các đối tượng Technology.
     */
    List<Technology> findAll();
}
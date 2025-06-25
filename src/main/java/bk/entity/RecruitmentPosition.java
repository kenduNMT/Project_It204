package bk.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "recruitment_position")
public class RecruitmentPosition {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    
    private String location;
    private String category;

    @Column(name = "min_salary")
    private Double minSalary;

    @Column(name = "max_salary")
    private Double maxSalary;

    @Column(name = "min_experience")
    private Integer minExperience;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "expired_date")
    private LocalDate expiredDate;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "recruitment_position_technology",
            joinColumns = @JoinColumn(name = "recruitment_position_id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id")
    )
    private Set<Technology> technologies = new HashSet<>();

    // Constructors
    public RecruitmentPosition() {}

    // Business methods
    public boolean isActive() {
        // Kiểm tra nếu chưa bị xóa và chưa hết hạn
        if (isDeleted != null && isDeleted) {
            return false;
        }

        // Nếu expiredDate là null thì vẫn active
        if (expiredDate == null) {
            return true;
        }

        // Kiểm tra ngày hết hạn
        return expiredDate.isAfter(LocalDate.now()) || expiredDate.isEqual(LocalDate.now());
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void addTechnology(Technology technology) {
        if (technologies == null) {
            technologies = new HashSet<>();
        }
        technologies.add(technology);
    }

    public void clearTechnologies() {
        if (technologies != null) {
            technologies.clear();
        }
    }
}
package bk.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recruitment_position")
public class RecruitmentPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;

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

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMinSalary() { return minSalary; }
    public void setMinSalary(Double minSalary) { this.minSalary = minSalary; }

    public Double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(Double maxSalary) { this.maxSalary = maxSalary; }

    public Integer getMinExperience() { return minExperience; }
    public void setMinExperience(Integer minExperience) { this.minExperience = minExperience; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getExpiredDate() { return expiredDate; }
    public void setExpiredDate(LocalDate expiredDate) { this.expiredDate = expiredDate; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Set<Technology> getTechnologies() { return technologies; }
    public void setTechnologies(Set<Technology> technologies) { this.technologies = technologies; }

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
package bk.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidate")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6-255 ký tự")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @Column(name = "phone", length = 20)
    private String phone;

    @Min(value = 0, message = "Kinh nghiệm không được âm")
    @Column(name = "experience", columnDefinition = "INT DEFAULT 0")
    private Integer experience = 0;

    @Pattern(regexp = "Nam|Nữ", message = "Giới tính phải là 'Nam' hoặc 'Nữ'")
    @Column(name = "gender", length = 10)
    private String gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "is_deleted", columnDefinition = "bit(1) DEFAULT false")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.CANDIDATE;

    public Candidate(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", experience=" + experience +
                ", gender='" + gender + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public enum Role {
        ADMIN, CANDIDATE
    }

    public enum Status {
        ACTIVE, INACTIVE
    }
}

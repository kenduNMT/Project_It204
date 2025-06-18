package bk.entity;


import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "technology")
public class Technology {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên công nghệ không được để trống")
    @Size(max = 100, message = "Tên công nghệ không được vượt quá 100 ký tự")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;


    public Technology(String name) {
        this.name = name;
        this.isDeleted = false;
    }

    // Helper methods
    public boolean isActive() {
        return !isDeleted;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    @Override
    public String toString() {
        return "Technology{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
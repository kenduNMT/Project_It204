package bk.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CandidateProfileUpdateDTO {

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100)
    private String name;

    @Size(max = 20)
    private String phone;

    @Pattern(regexp = "Nam|Nữ", message = "Giới tính phải là 'Nam' hoặc 'Nữ'")
    private String gender;

    @Min(0)
    private Integer experience;

    private String description;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

}

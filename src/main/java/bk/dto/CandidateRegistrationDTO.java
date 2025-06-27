package bk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;

@NoArgsConstructor
@Setter
@Getter
public class CandidateRegistrationDTO {

    // Getters and Setters
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6-50 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    @Pattern(regexp = "Nam|Nữ", message = "Giới tính phải là 'Nam' hoặc 'Nữ'")
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @Min(value = 0, message = "Kinh nghiệm không được âm")
    private Integer experience = 0;

    /**
     * Kiểm tra mật khẩu và xác nhận mật khẩu có khớp không
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
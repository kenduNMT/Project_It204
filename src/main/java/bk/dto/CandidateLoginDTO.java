package bk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class CandidateLoginDTO {

    // Getters and Setters
    @Setter
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Setter
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private final boolean rememberMe = false;

}
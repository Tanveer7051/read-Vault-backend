package in.ReadVault.DTO;

import lombok.Data;

@Data
public class VerifyOtpDTO {
    private String email;
    private Long otp;
}

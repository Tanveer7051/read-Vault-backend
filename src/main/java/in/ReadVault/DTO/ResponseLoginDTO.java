package in.ReadVault.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseLoginDTO {
    private String accessToken;
    private String refreshToken;
}

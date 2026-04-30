package in.ReadVault.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationDTO {
    private long id;
    private String city;
    private String district;
    private String state;
    private String country;
    private long pincode;

    private long userId;
}
package in.ReadVault.Controller;

import in.ReadVault.DTO.UserDTO;
import in.ReadVault.Service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId){
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "userId", userId,
                "message", "User Successfully Deleted",
                "status" , HttpStatus.OK,
                "date", LocalDate.now()
        ));
    }
}

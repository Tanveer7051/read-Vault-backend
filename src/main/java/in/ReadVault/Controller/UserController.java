package in.ReadVault.Controller;

import in.ReadVault.DTO.UpdateUserDTO;
import in.ReadVault.DTO.UserDTO;
import in.ReadVault.Entity.User;
import in.ReadVault.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{userId}/toggle-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserRole(
            @PathVariable Long userId
    ) {

        UserDTO updatedUser =
                userService.toggleUserRole(userId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "User role updated successfully",
                        "data", updatedUser
                )
        );
    }
    @GetMapping("/all")
    public List<UserDTO> getAllUser() {
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(

            Authentication authentication,

            @Valid @ModelAttribute UpdateUserDTO updateUserDTO,

            @RequestParam(value = "image", required = false)
            MultipartFile image
    ) {

        Long userId =
                ((User) authentication.getPrincipal()).getId();

        UserDTO updatedUser =
                userService.updateUser(userId, updateUserDTO, image);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "message", "User Successfully Updated",
                "status", HttpStatus.OK,
                "date", LocalDate.now(),
                "user", updatedUser
        ));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long userId
    ) {

        userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "userId", userId,
                "message", "User Successfully Deleted",
                "status", HttpStatus.OK,
                "date", LocalDate.now()
        ));
    }
}
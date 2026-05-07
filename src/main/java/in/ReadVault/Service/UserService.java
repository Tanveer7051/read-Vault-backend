package in.ReadVault.Service;

import com.cloudinary.Cloudinary;
import in.ReadVault.DTO.SignUpDTO;
import in.ReadVault.DTO.UpdateUserDTO;
import in.ReadVault.DTO.UserDTO;
import in.ReadVault.Entity.Role;
import in.ReadVault.Entity.User;
import in.ReadVault.GlobalExceptionHandling.BadRequestExceptions;
import in.ReadVault.GlobalExceptionHandling.UserAlreadyRegisteredException;
import in.ReadVault.GlobalExceptionHandling.UserNotFoundException;
import in.ReadVault.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary;

    public UserDTO addUser(SignUpDTO signUpDTO) {

        User user = modelMapper.map(signUpDTO, User.class);

        user.setRole(Role.USER);

        userRepository.save(user);

        return modelMapper.map(user, UserDTO.class);
    }

    public List<UserDTO> getAllUser() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    public UserDTO getUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User Not Found With Id: " + id));

        return modelMapper.map(user, UserDTO.class);
    }

    public UserDTO updateUser(
            Long userId,
            UpdateUserDTO updateUserDTO,
            MultipartFile image
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User Not Found"));

        // FIRSTNAME
        if (updateUserDTO.getFirstname() != null &&
                !updateUserDTO.getFirstname().trim().isEmpty()) {

            user.setFirstname(updateUserDTO.getFirstname());
        }

        // LASTNAME
        if (updateUserDTO.getLastname() != null &&
                !updateUserDTO.getLastname().trim().isEmpty()) {

            user.setLastname(updateUserDTO.getLastname());
        }

        // USERNAME
        if (updateUserDTO.getUsername() != null &&
                !updateUserDTO.getUsername().trim().isEmpty()) {

            boolean exists = userRepository.existsByUsername(
                    updateUserDTO.getUsername());

            if (exists &&
                    !user.getUsername()
                            .equals(updateUserDTO.getUsername())) {

                throw new UserAlreadyRegisteredException(
                        "Username already taken");
            }

            user.setUsername(updateUserDTO.getUsername());
        }

        // PROFILE IMAGE
        if (image != null && !image.isEmpty()) {

            try {

                // VALIDATE IMAGE
                if (!image.getContentType().startsWith("image/")) {

                    throw new BadRequestExceptions(
                            "Only image files are allowed");
                }

                // DELETE OLD IMAGE FROM CLOUDINARY
                if (user.getProfileImagePublicId() != null &&
                        !user.getProfileImagePublicId().isEmpty()) {

                    cloudinary.uploader().destroy(
                            user.getProfileImagePublicId(),
                            Map.of("resource_type", "image")
                    );
                }

                // UPLOAD NEW IMAGE
                Map<String, Object> uploadResult =
                        cloudinary.uploader().upload(
                                image.getBytes(),
                                Map.of("resource_type", "image")
                        );

                String imageUrl =
                        uploadResult.get("secure_url").toString();

                String publicId =
                        uploadResult.get("public_id").toString();

                user.setProfileImage(imageUrl);
                user.setProfileImagePublicId(publicId);

            } catch (IOException e) {

                throw new RuntimeException(
                        "Failed to upload profile image");
            }
        }

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDTO.class);
    }

    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User Not Found With This Id : "
                                        + userId));

        try {

            // DELETE PROFILE IMAGE FROM CLOUDINARY
            if (user.getProfileImagePublicId() != null &&
                    !user.getProfileImagePublicId().isEmpty()) {

                cloudinary.uploader().destroy(
                        user.getProfileImagePublicId(),
                        Map.of("resource_type", "image")
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to delete profile image");
        }

        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with this username "
                                        + email));
    }

    public User getUserByUserId(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with this id "
                                        + id));
    }
}
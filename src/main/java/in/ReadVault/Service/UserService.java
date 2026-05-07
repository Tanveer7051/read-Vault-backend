package in.ReadVault.Service;

import in.ReadVault.DTO.SignUpDTO;
import in.ReadVault.DTO.UserDTO;
import in.ReadVault.Entity.Role;
import in.ReadVault.Entity.User;
import in.ReadVault.GlobalExceptionHandling.UserNotFoundException;
import in.ReadVault.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    public UserDTO addUser(SignUpDTO signUpDTO) {
        User user= modelMapper.map(signUpDTO, User.class);
        user.setRole(Role.USER);
        userRepository.save(user);

        return modelMapper.map(user,UserDTO.class);
    }

    public List<UserDTO> getAllUser() {
        List<User> users=userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    public UserDTO getUser(Long id) {
        User user = (userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found With Id: " + id)));

        return modelMapper.map(user,UserDTO.class);
    }

    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                        .orElseThrow(()-> new UserNotFoundException("User Not Found With This Id : "+ userId));
        userRepository.deleteById(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found with this username "+ email));
    }


    public User getUserByUserId(Long id){
        User user =userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("User not found with this id "+ id));
        return user;
    }
}

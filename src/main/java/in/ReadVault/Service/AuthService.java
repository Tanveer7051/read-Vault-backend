package in.ReadVault.Service;

import in.ReadVault.DTO.*;
import in.ReadVault.Entity.EmailVerification;
import in.ReadVault.Entity.Role;
import in.ReadVault.Entity.User;
import in.ReadVault.GlobalExceptionHandling.BadRequestExceptions;
import in.ReadVault.GlobalExceptionHandling.UserAlreadyRegisteredException;
import in.ReadVault.GlobalExceptionHandling.UserNotFoundException;
import in.ReadVault.Repository.EmailRepository;
import in.ReadVault.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final EmailRepository emailRepository;

    public String addUser(SignUpDTO signUpDTO) {

        if (userRepository.findByEmail(signUpDTO.getEmail()).isPresent()) {
            throw new UserAlreadyRegisteredException(
                    "This email is already registered"
            );
        }

        if (userRepository.findByUsername(signUpDTO.getUsername()).isPresent()) {
            throw new UserAlreadyRegisteredException(
                    "This username is already taken"
            );
        }

        EmailVerification emailVerificationEntity = new EmailVerification();

        emailVerificationEntity.setFirstname(signUpDTO.getFirstname());
        emailVerificationEntity.setLastname(signUpDTO.getLastname());
        emailVerificationEntity.setUsername(signUpDTO.getUsername());
        emailVerificationEntity.setEmail(signUpDTO.getEmail());

        emailVerificationEntity.setPassword(
                passwordEncoder.encode(signUpDTO.getPassword())
        );

        emailVerificationEntity.setRole(
                signUpDTO.getRole() != null
                        ? signUpDTO.getRole()
                        : Role.USER
        );

        emailService.generateOtp(emailVerificationEntity);

        return "OTP sent successfully to your email";
    }

    public UserDTO verifyAndCreateUser(VerifyOtpDTO verifyOtpDTO) {

        EmailVerification emailVerificationEntity = emailRepository.findByEmail(verifyOtpDTO.getEmail())
                .orElseThrow(() ->
                        new BadRequestExceptions("Email not found")
                );

        boolean verified = emailService.verifyOtp(verifyOtpDTO.getEmail(), verifyOtpDTO.getOtp());

        if (!verified) {
            throw new BadRequestExceptions(
                    "Invalid or expired OTP"
            );
        }

        User user = new User();

        user.setFirstname(emailVerificationEntity.getFirstname());
        user.setLastname(emailVerificationEntity.getLastname());
        user.setUsername(emailVerificationEntity.getUsername());
        user.setEmail(emailVerificationEntity.getEmail());
        user.setPassword(emailVerificationEntity.getPassword());
        user.setRole(emailVerificationEntity.getRole());

        User savedUser = userRepository.save(user);

        emailRepository.delete(emailVerificationEntity);
        emailService.sendWelcomeMail(user.getEmail(),user.getFirstname(),user.getLastname());

        return modelMapper.map(savedUser, UserDTO.class);
    }

    public ResponseLoginDTO login(LoginRequestDTO loginRequestDTO) {

        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with this email "
                                        + loginRequestDTO.getEmail()
                        )
                );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()
                )
        );

        User loginUser = (User) authentication.getPrincipal();

        String accessToken =
                jwtTokenService.generateAccessToken(loginUser);

        String refreshToken =
                jwtTokenService.generateRefreshToken(loginUser);

        return new ResponseLoginDTO(accessToken, refreshToken);
    }

    public ResponseLoginDTO refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BadRequestExceptions("Invalid Refresh Token");
        }

        if (!jwtTokenService.isRefreshToken(refreshToken)) {
            throw new BadRequestExceptions("Invalid Refresh Token");
        }

        Long id = jwtTokenService.getIdFromToken(refreshToken);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User Not Found")
                );

        String accessToken =
                jwtTokenService.generateAccessToken(user);

        return new ResponseLoginDTO(accessToken, refreshToken);
    }
}

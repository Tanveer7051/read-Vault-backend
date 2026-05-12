package in.ReadVault.Controller;

import in.ReadVault.DTO.*;
import in.ReadVault.GlobalExceptionHandling.BadRequestExceptions;
import in.ReadVault.Service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<Map<String, String>> addUser(
            @Valid @RequestBody SignUpDTO signUpDTO
    ) {
        logger.info("request invoked");
        String message = authService.addUser(signUpDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", message)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<UserDTO> verifyOtp(@RequestBody VerifyOtpDTO verifyOtpDTO
                                             ) {

        return ResponseEntity.ok(
                authService.verifyAndCreateUser(verifyOtpDTO)
        );
    }

    @PostMapping("/login")
    public ResponseLoginDTO login(
            @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {

        ResponseLoginDTO responseLoginDTO =
                authService.login(loginRequestDTO);

        Cookie cookie = new Cookie(
                "refreshToken",
                responseLoginDTO.getRefreshToken()
        );

        cookie.setHttpOnly(true);

        response.addCookie(cookie);

        return responseLoginDTO;
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseLoginDTO> refreshAccessToken(
            HttpServletRequest request
    ) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new BadRequestExceptions("No cookies found");
        }

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie ->
                        "refreshToken".equals(cookie.getName())
                )
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new BadRequestExceptions(
                                "Invalid Refresh Token"
                        )
                );

        return ResponseEntity.ok(
                authService.refresh(refreshToken)
        );
    }
}

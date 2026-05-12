package in.ReadVault.Service;

import in.ReadVault.Entity.EmailVerification;
import in.ReadVault.Repository.EmailRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;

    public void generateOtp(EmailVerification emailVerificationEntity) {

        long otp = 100000 + (long) (Math.random() * 900000);

        Optional<EmailVerification> existingEmail =
                emailRepository.findByEmail(emailVerificationEntity.getEmail());

        if (existingEmail.isPresent()) {

            EmailVerification existing = existingEmail.get();

            existing.setFirstname(emailVerificationEntity.getFirstname());
            existing.setLastname(emailVerificationEntity.getLastname());
            existing.setUsername(emailVerificationEntity.getUsername());
            existing.setPassword(emailVerificationEntity.getPassword());
            existing.setRole(emailVerificationEntity.getRole());

            existing.setOtp(otp);

            existing.setExpiryTime(
                    LocalDateTime.now().plusMinutes(5)
            );

            existing.setVerified(false);

            emailRepository.save(existing);

        } else {

            emailVerificationEntity.setOtp(otp);

            emailVerificationEntity.setExpiryTime(
                    LocalDateTime.now().plusMinutes(5)
            );

            emailVerificationEntity.setVerified(false);

            emailRepository.save(emailVerificationEntity);
        }

        sendOtpMail(emailVerificationEntity.getEmail(), otp);
    }

    private void sendOtpMail(String email, long otp) {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("readvault.library@gmail.com");
            helper.setTo(email);

            helper.setSubject("Verify Your ReadVault Account 📚");

            String html = """
                <div style="
                    margin:0;
                    padding:40px 20px;
                    background:#f4f7fb;
                    font-family:Arial,sans-serif;
                ">

                    <div style="
                        max-width:600px;
                        margin:auto;
                        background:white;
                        border-radius:20px;
                        overflow:hidden;
                        box-shadow:0 8px 25px rgba(0,0,0,0.08);
                    ">

                        <!-- HEADER -->
                        <div style="
                            background:linear-gradient(135deg,#4A90E2,#6A5ACD);
                            padding:35px;
                            text-align:center;
                            color:white;
                        ">

                            <h1 style="
                                margin:0;
                                font-size:32px;
                                letter-spacing:1px;
                            ">
                                ReadVault 📚
                            </h1>

                            <p style="
                                margin-top:10px;
                                font-size:15px;
                                opacity:0.9;
                            ">
                                Your Digital Reading Companion
                            </p>
                        </div>

                        <!-- BODY -->
                        <div style="padding:40px 35px; color:#333;">

                            <h2 style="
                                margin-top:0;
                                font-size:24px;
                                color:#222;
                            ">
                                Email Verification
                            </h2>

                            <p style="
                                font-size:16px;
                                line-height:1.7;
                                color:#555;
                            ">
                                Welcome to ReadVault!  
                                Use the OTP below to verify your email address
                                and complete your registration.
                            </p>

                            <!-- OTP BOX -->
                            <div style="
                                margin:35px 0;
                                text-align:center;
                            ">

                                <div style="
                                    display:inline-block;
                                    background:#f3f6ff;
                                    color:#4A90E2;
                                    padding:18px 35px;
                                    border-radius:14px;
                                    font-size:34px;
                                    font-weight:bold;
                                    letter-spacing:8px;
                                    border:2px dashed #4A90E2;
                                ">
                                    %s
                                </div>

                            </div>

                            <p style="
                                font-size:15px;
                                color:#666;
                                line-height:1.7;
                            ">
                                ⏳ This OTP will expire in
                                <strong>5 minutes</strong>.
                            </p>

                            <p style="
                                font-size:15px;
                                color:#666;
                                line-height:1.7;
                            ">
                                If you didn’t request this verification,
                                you can safely ignore this email.
                            </p>

                            <!-- QUOTE -->
                            <div style="
                                margin-top:35px;
                                padding:20px;
                                background:#fafafa;
                                border-left:4px solid #6A5ACD;
                                border-radius:10px;
                                color:#555;
                                font-style:italic;
                            ">
                                “A reader lives a thousand lives before he dies.”
                            </div>

                        </div>

                        <!-- FOOTER -->
                        <div style="
                            background:#fafafa;
                            padding:20px;
                            text-align:center;
                            font-size:13px;
                            color:#888;
                            border-top:1px solid #eee;
                        ">

                            © 2026 ReadVault • All Rights Reserved

                        </div>

                    </div>

                </div>
                """.formatted(otp);

            helper.setText(html, true);

            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyOtp(String email, Long otp) {

        Optional<EmailVerification> existingEmail = emailRepository.findByEmail(email);

        if (existingEmail.isEmpty()) {
            return false;
        }

        EmailVerification emailVerificationEntity = existingEmail.get();

        // OTP EXPIRED
        if (emailVerificationEntity.getExpiryTime().isBefore(LocalDateTime.now())) {

            emailRepository.delete(emailVerificationEntity);

            return false;
        }

        // OTP DOES NOT MATCH
        if (!emailVerificationEntity.getOtp().equals(otp)) {
            return false;
        }

        // OTP VERIFIED
        emailVerificationEntity.setVerified(true);

        emailRepository.save(emailVerificationEntity);

        return true;
    }

    public void sendWelcomeMail(String email, String firstname, String lastname) {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("readvault.library@gmail.com");

            helper.setTo(email);

            helper.setSubject("Welcome to ReadVault 📚");

            String html = """
                <div style="
                    margin:0;
                    padding:40px 20px;
                    background:#f4f7fb;
                    font-family:Arial,sans-serif;
                ">

                    <div style="
                        max-width:650px;
                        margin:auto;
                        background:white;
                        border-radius:20px;
                        overflow:hidden;
                        box-shadow:0 8px 25px rgba(0,0,0,0.08);
                    ">

                        <!-- HEADER -->
                        <div style="
                            background:linear-gradient(135deg,#6A5ACD,#4A90E2);
                            padding:40px;
                            text-align:center;
                            color:white;
                        ">

                            <h1 style="
                                margin:0;
                                font-size:34px;
                                letter-spacing:1px;
                            ">
                                Welcome to ReadVault 📚
                            </h1>

                            <p style="
                                margin-top:12px;
                                font-size:16px;
                                opacity:0.9;
                            ">
                                Your Digital Library Journey Starts Here
                            </p>

                        </div>

                        <!-- BODY -->
                        <div style="
                            padding:45px 35px;
                            color:#333;
                        ">

                            <h2 style="
                                margin-top:0;
                                font-size:26px;
                                color:#222;
                            ">
                                Hello %s 👋
                            </h2>

                            <p style="
                                font-size:16px;
                                line-height:1.8;
                                color:#555;
                            ">
                                Your ReadVault account has been
                                <strong>successfully created</strong>.
                            </p>

                            <p style="
                                font-size:16px;
                                line-height:1.8;
                                color:#555;
                            ">
                                We’re excited to welcome you into our
                                growing digital reading community where
                                books, knowledge, and stories come together.
                            </p>

                            <!-- FEATURE BOX -->
                            <div style="
                                margin:35px 0;
                                padding:25px;
                                background:#f8f9ff;
                                border-radius:16px;
                                border:1px solid #e5e9ff;
                            ">

                                <h3 style="
                                    margin-top:0;
                                    color:#4A90E2;
                                ">
                                    What You Can Explore 🚀
                                </h3>

                                <p style="
                                    margin:10px 0;
                                    color:#555;
                                    line-height:1.7;
                                ">
                                    • Discover new books 📖<br>
                                    • Reserve and manage books 📚<br>
                                    • Explore categories and collections 🔍<br>
                                    • Enjoy your personalized reading space ✨
                                </p>

                            </div>

                            <!-- QUOTE -->
                            <div style="
                                margin-top:30px;
                                padding:22px;
                                background:#fafafa;
                                border-left:5px solid #6A5ACD;
                                border-radius:10px;
                                color:#555;
                                font-style:italic;
                                line-height:1.8;
                            ">
                                “A reader lives a thousand lives before he dies.”
                            </div>

                            <p style="
                                margin-top:35px;
                                font-size:16px;
                                color:#555;
                            ">
                                Happy Reading 📖
                            </p>

                            <p style="
                                font-size:16px;
                                font-weight:bold;
                                color:#333;
                            ">
                                — Team ReadVault
                            </p>

                        </div>

                        <!-- FOOTER -->
                        <div style="
                            background:#fafafa;
                            padding:20px;
                            text-align:center;
                            font-size:13px;
                            color:#888;
                            border-top:1px solid #eee;
                        ">

                            © 2026 ReadVault • All Rights Reserved

                        </div>

                    </div>

                </div>
                """.formatted(firstname + " " + lastname);

            helper.setText(html, true);

            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 86400000)
    public void deleteExpiredOtps() {

        List<EmailVerification> expiredEmailVerifications = emailRepository
                .findByExpiryTimeBefore(LocalDateTime.now());

        emailRepository.deleteAll(expiredEmailVerifications);
    }
}

package in.ReadVault.Filter;

import in.ReadVault.Entity.User;
import in.ReadVault.GlobalExceptionHandling.BadRequestExceptions;
import in.ReadVault.Service.JwtTokenService;
import in.ReadVault.Service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{

        String headerToken=request.getHeader("Authorization");
        if(headerToken== null || !headerToken.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }

        String token=headerToken.substring(7);
        if(!jwtTokenService.isAccessToken(token)){
            throw new BadRequestExceptions("Invalid Access Token");
        }
        Long id=jwtTokenService.getIdFromToken(token);
        if(id !=null && SecurityContextHolder.getContext().getAuthentication()==null){
            User user =userService.getUserByUserId(id);
            UsernamePasswordAuthenticationToken authenticateToken=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
            authenticateToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authenticateToken);
        }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
        {
          "error": "Unauthorized",
          "message": "Invalid or expired token"
        }
        """);
            return;
        }
        filterChain.doFilter(request,response);
    }
}

package br.com.attendant.config;
import br.com.attendant.service.EnterpriseDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private EnterpriseDetailsService enterpriseDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            final String path = request.getServletPath();

            if (path.startsWith("/login") ||
                    path.startsWith("/codigo-verificacao") ||
                    path.equals("/enterprise")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            String username = null;
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = enterpriseDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            resolver.resolveException(request, response, null, ex);
        } catch (ExpiredJwtException ex) {
            resolver.resolveException(request, response, null, new BusinessException(ExceptionEnum.GENERIC, ex.getMessage()));
        } catch (JwtException | IllegalArgumentException ex) {
            resolver.resolveException(request, response, null, new BusinessException(ExceptionEnum.GENERIC, ex.getMessage()));
        } catch (Exception ex) {
            resolver.resolveException(request, response, null, new BusinessException(ExceptionEnum.GENERIC));
        }
    }
}
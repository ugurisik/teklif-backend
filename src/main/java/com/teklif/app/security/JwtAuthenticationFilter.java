package com.teklif.app.security;

import com.teklif.app.repository.UserTenantRepository;
import com.teklif.app.util.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserTenantRepository userTenantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtil.extractUsername(jwt);
            final String jwtTenantId = jwtUtil.extractTenantId(jwt);
            final String userId = jwtUtil.extractUserId(jwt);

            // Header'dan tenant kontrol et (kullanıcının seçtiği tenant)
            final String headerTenantId = request.getHeader("X-Tenant-Id");

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    String effectiveTenantId = jwtTenantId;

                    // Öncelik sırası:
                    // 1. Header'daki tenant (kullanıcı manuel seçti)
                    // 2. user_tenants tablosundaki default tenant
                    // 3. JWT'deki tenant (fallback)

                    if (headerTenantId != null && !headerTenantId.isEmpty() && userId != null) {
                        // Kullanıcının bu tenant'a erişimi var mı?
                        if (userTenantRepository.existsByUserIdAndTenantId(userId, headerTenantId)) {
                            effectiveTenantId = headerTenantId;
                        }
                    } else if (userId != null) {
                        // user_tenants tablosundan default tenant'ı çek
                        Optional<com.teklif.app.entity.UserTenant> defaultUserTenant =
                                userTenantRepository.findDefaultByUserId(userId);

                        if (defaultUserTenant.isPresent()) {
                            effectiveTenantId = defaultUserTenant.get().getTenantId();
                        }
                    }

                    if (effectiveTenantId != null) {
                        TenantContext.setTenantId(effectiveTenantId);
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request, response);
    }
}
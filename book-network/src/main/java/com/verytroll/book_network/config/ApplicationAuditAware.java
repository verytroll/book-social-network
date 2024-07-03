package com.verytroll.book_network.config;

import com.verytroll.book_network.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<Integer> {
    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Integer> result;
        if(authentication == null ||
           !authentication.isAuthenticated() ||
           authentication instanceof AnonymousAuthenticationToken) {
            result = Optional.empty();
        } else {
            User userPrincipal = (User)authentication.getPrincipal();
            result = Optional.ofNullable(userPrincipal.getId());
        }
        return result;
    }
}

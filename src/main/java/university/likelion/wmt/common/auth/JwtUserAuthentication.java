package university.likelion.wmt.common.auth;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtUserAuthentication extends AbstractAuthenticationToken {
    private Long userId;

    public JwtUserAuthentication(Collection<? extends GrantedAuthority> authorities, Long userId) {
        super(authorities);
        this.userId = userId;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}

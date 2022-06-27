package org.orcid.core.oauth;

import java.util.Collection;

import org.orcid.core.constants.RevokeReason;
import org.orcid.core.oauth.service.OrcidTokenStore;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * @author Angel Montenegro
 * */
public interface OrcidRandomValueTokenServices extends AuthorizationServerTokenServices {

    OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException;

    OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException;

    int getWriteValiditySeconds();

    int getReadValiditySeconds();

    void setOrcidtokenStore(OrcidTokenStore orcidtokenStore);

    void setCustomTokenEnhancer(TokenEnhancer customTokenEnhancer);
    
    boolean longLifeTokenExist(String clientId, String userId, Collection<String> scopes);

    OAuth2AccessToken createRevokedAccessToken(OAuth2Authentication authentication, RevokeReason revokeReason) throws AuthenticationException;
    
}

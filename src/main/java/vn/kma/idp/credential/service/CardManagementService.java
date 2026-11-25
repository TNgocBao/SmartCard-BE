package vn.kma.idp.credential.service;

import java.util.*;
import org.springframework.stereotype.Service;
import vn.kma.idp.credential.repository.CardManagementRepository;
import vn.kma.idp.security.*;
import vn.kma.idp.shared.ApiException;

@Service
public class CardManagementService {
    private final CardManagementRepository repository;
    private final AccessService access;
    public CardManagementService(CardManagementRepository repository,AccessService access){this.repository=repository;this.access=access;}
    public Map<String,Object> list(UserPrincipal user,String account,int page,boolean history) {
        if(page<0||page>10000)throw new ApiException(400,"INVALID_PAGE");
        String target=account==null?user.accountId():account;
        if(!target.equals(user.accountId()))access.requireAccount(user,"credential.manage",target);
        return Map.of("data",history?repository.history(target,page*50):repository.cards(target,page*50),"page",page);
    }
}

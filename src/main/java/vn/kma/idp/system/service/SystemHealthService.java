package vn.kma.idp.system.service;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import vn.kma.idp.system.repository.SystemHealthRepository;

@Service
public class SystemHealthService {
    private final SystemHealthRepository repository;
    public SystemHealthService(SystemHealthRepository repository){this.repository=repository;}
    public boolean ready(){
        try{return repository.databaseReady();}
        catch(DataAccessException unavailable){return false;}
    }
}

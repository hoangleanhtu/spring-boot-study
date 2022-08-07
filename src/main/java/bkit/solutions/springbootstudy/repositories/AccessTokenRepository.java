package bkit.solutions.springbootstudy.repositories;

import bkit.solutions.springbootstudy.entities.AccessTokenInfo;
import org.springframework.data.repository.CrudRepository;

public interface AccessTokenRepository extends CrudRepository<AccessTokenInfo, String> {
}

package com.mnao.mfp.cr.repository;

import com.mnao.mfp.cr.entity.Dealers;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealerRepository extends CrudRepository<Dealers, String> {

}

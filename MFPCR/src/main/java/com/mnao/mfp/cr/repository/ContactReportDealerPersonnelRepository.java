package com.mnao.mfp.cr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mnao.mfp.cr.entity.ContactReportDealerPersonnel;

@Repository
public interface ContactReportDealerPersonnelRepository extends JpaRepository<ContactReportDealerPersonnel, Long> {

}

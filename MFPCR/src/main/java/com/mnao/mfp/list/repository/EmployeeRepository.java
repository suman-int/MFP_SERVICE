package com.mnao.mfp.list.repository;

import com.mnao.mfp.cr.entity.Employees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, String> {
    @Query("SELECT e FROM Employees e WHERE e.PRSN_TYPE_CD = ?1 and e.JOB_CD = ?2")
    List<Employees> findAllByPrsnTypeCdAndJobCd(String personCode, String jobCode);
}

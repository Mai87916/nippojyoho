package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, String> {
	// 特定の従業員の日報を取得するメソッド
    List<Report> findByEmployee(Employee employee);
    
 // 指定の従業員と日付に一致する日報が存在するかどうか
    boolean existsByEmployeeAndReportDate(Employee employee, LocalDate reportDate);
}
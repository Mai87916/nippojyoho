package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ReportService(EmployeeRepository employeeRepository, ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
        this.employeeRepository = employeeRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        // データ重複チェック
        if (existsByEmployeeAndDate(report.getEmployee(),report.getReportDate())) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }
    
    // 日報更新
    @Transactional
    public ErrorKinds update(Report report, Report reportBk) {
    	
    	// データ重複チェック
        if (!report.getReportDate().equals(reportBk.getReportDate()) && existsByEmployeeAndDate(report.getEmployee(),report.getReportDate())) {
            return ErrorKinds.DATECHECK_ERROR;
        }

    	report.setTitle(report.getTitle());
    	report.setReportDate(report.getReportDate());
        report.setDeleteFlg(false);
        
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(String id) {

        Report report = findByReport(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 該当社員の日報のみ検索
    public List<Report> findByReports(String employeeCode) {
    	// 従業員コードで従業員を取得
    	// findByIdで検索
        Optional<Employee> option = employeeRepository.findById(employeeCode);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        
	    // 従業員の日報を取得
	    return reportRepository.findByEmployee(employee);
           
    }
    
    public boolean existsByEmployeeAndDate(Employee employee, LocalDate reportDate) {
        return reportRepository.existsByEmployeeAndReportDate(employee, reportDate);
    }
    
 // 該当社員の日報のみ検索
    public Report findByReport(String id) {
    	// findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        
	    // 従業員の日報を取得
	    return report;
           
    }

	public List<Report> findByEmployee(Employee employee) {
		// TODO 自動生成されたメソッド・スタブ
		List<Report> reports = reportRepository.findByEmployee(employee);
        
	    // 従業員の日報を取得
	    return reports;
	    
	}
}

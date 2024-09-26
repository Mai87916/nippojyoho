package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class EmployeeReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeReportController(ReportService reportService,EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model,@AuthenticationPrincipal UserDetail userDetail) {

    	if ("管理者".equals(userDetail.getEmployee().getRole().getValue())) {
    		// 管理者
    		model.addAttribute("listSize", reportService.findAll().size());
            model.addAttribute("reportList", reportService.findAll());
    	}else {
    		// 一般
    		model.addAttribute("listSize", reportService.findByEmployee(userDetail.getEmployee()).size());
            model.addAttribute("reportList", reportService.findByEmployee(userDetail.getEmployee()));
    	}

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable("id") String id, Model model) {

        model.addAttribute("report", reportService.findByReport(id));
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {

    	model.addAttribute("loginuser", userDetail.getEmployee().getName());
        return "reports/new";
    }
    
    // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String update(@PathVariable("id") String id, Model model) {

    	Report report = reportService.findByReport(id);
        model.addAttribute("report", report);
        return "reports/update";
    }
    

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model,@AuthenticationPrincipal UserDetail userDetail) {

    	// ログイン中のユーザー情報を取得
        Employee employee = employeeService.findByCode(userDetail.getEmployee().getCode());

        // 入力チェック
        if (res.hasErrors()) {
            return create(report, userDetail, model); // 入力エラーがある場合は再度登録画面に戻す
        }

        // 日報の従業員情報をセット（ログインしている従業員を使用）
        report.setEmployee(employee);

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
        	ErrorKinds result = reportService.save(report); // 日報を保存

            // エラーメッセージが含まれている場合
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report, userDetail, model); // エラー時は再度登録画面に戻す
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(report,userDetail, model);
        }

        return "redirect:/reports";
    }
    
    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable("id") String id,@Validated @ModelAttribute Report report,  BindingResult res, Model model) {
    	 
    	Report reportBk = reportService.findByReport(id);
        report.setEmployee(reportBk.getEmployee());
        report.setCreatedAt(reportBk.getCreatedAt());
        
    	// 入力チェック
        if (res.hasErrors()) {
//        	return update(id, model);
        	return "reports/update";
        }
        
    	try {
    		
    		ErrorKinds result = reportService.update(report, reportBk); // 日報更新

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
//                return update(id, model);
                return "reports/update";
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
//            return update(id, model);
            return "reports/update";
        }
    	
    	return "redirect:/reports";
    }
    
    

    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable String id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = reportService.delete(id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findByReport(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

}

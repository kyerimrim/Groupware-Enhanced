package com.giga.gw.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.giga.gw.util.ProfileImageValidator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.giga.gw.dto.DepartmentDto;
import com.giga.gw.dto.EmployeeDto;
import com.giga.gw.repository.IApprovalDao;
import com.giga.gw.repository.IDeptManagementDao;
import com.giga.gw.repository.IEmployeeDao;
import com.giga.gw.service.IEmployeeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/hrManagement")
public class HRManagementController {
	
	private final IApprovalDao approvalDao;
	private final IEmployeeDao employeeDao;
	private final IDeptManagementDao deptManagementDao;
	
	// 마이페이지 - 사원 값 가져오기
	@GetMapping("/mypage.do")
	public String mypage(Model model, HttpSession session) {
		EmployeeDto loginDto = (EmployeeDto)session.getAttribute("loginDto");
		log.info("로그인 사번 : {}", loginDto);
		model.addAttribute("employee", loginDto);
		model.addAttribute("profileImage", employeeDao.getProfileImage(String.valueOf(loginDto.getEmpno())));
		return "mypage";
	}
	
	// 사원 등록 - 페이지 요청
	@GetMapping("/employeeAdd.do")
	public String DeptManagement(Model model) {
		List<DepartmentDto> deptList = deptManagementDao.deptSelect();
		if (deptList != null && !deptList.isEmpty()) {
	        DepartmentDto firstDept = deptList.get(0);
	        log.info("첫번째 부서 정보 - deptno: {}, deptname: {}", 
	                firstDept.getDeptno(), firstDept.getDeptname());
	    } else {
	        log.info("조회된 부서 정보가 없습니다.");
	    }
		
		model.addAttribute("deptList", deptList);
		log.info("모델에 'deptList' 속성 추가 완료");
		return "employeeAdd";
	}
	
	// 사원 등록 - 사원번호 불러오기
	@GetMapping("/nextEmpno.do")
	@ResponseBody
	public String getNextEmpno(@RequestParam String hiredate) {
	    log.info("HRManagementController getNextEmpno Get 호출");
	    String employee = employeeDao.getNextEmpno(hiredate);
	    log.info("입사일자 불러오기{}",hiredate);
	    return employee != null ? employee : "";
	}
	
	// 사원 등록 - 사원 등록
	@PostMapping(value = "/employeeAdd.do", consumes = "multipart/form-data",
			produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> employeeResist(@RequestPart("employee") Map<String, Object> map,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
			HttpSession session) throws java.io.IOException {
		EmployeeDto loginDto = (EmployeeDto)session.getAttribute("loginDto");
		if (loginDto == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
		try {
			map.put("profileImage", ProfileImageValidator.encode(profileImage));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		map.put("create_emp", loginDto.getEmpno());
		String empno = (String) map.get("empno");
		map.put("password", empno);
		return employeeDao.insertEmployee(map) == 1
				? ResponseEntity.ok("true")
				: ResponseEntity.status(500).body("사원 등록에 실패했습니다.");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseBody
	public ResponseEntity<String> uploadTooLarge() {
		return ResponseEntity.status(413)
				.contentType(org.springframework.http.MediaType.parseMediaType("text/plain;charset=UTF-8"))
				.body("이미지는 5MB 이하로 선택해주세요.");
	}
	
	//조직도
	@ResponseBody
	@GetMapping("/tree.do")
	public List<Map<String, Object>> tree() {
		return approvalDao.getOrganizationTree();
	}
	
	@GetMapping("/hrManagement.do")
	public String hrManagerment() {
		return "hrManagement";
	}
	
	// 사원 리스트
	@GetMapping("/employeeList.do")
	@ResponseBody
	public List<EmployeeDto> employeeList() {
		log.info("사원 리스트 조회");
		return employeeDao.employeeList();
	}
	
	// 사원 조회
	@GetMapping("/empGet.do")
	@ResponseBody
	public EmployeeDto getOneEmp(@RequestParam("seq") String seq){
		log.info("HRManagementController getOneEmp GET 요청");
		EmployeeDto emp = employeeDao.getEmpno(seq);
		return emp;
	}

}

package com.mnao.mfp.cr.controller;

import com.mnao.mfp.cr.entity.ContactReportInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mnao.mfp.cr.Service.ContactReportServiceImpl;
import com.mnao.mfp.cr.dto.ContactReportDto;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path="/ContactReport")
public class ReportController {

	@Autowired(required = true)
	private ContactReportServiceImpl contactReportService;

	@PostMapping(value ="/submitReport")
    public String submitReportData(@Valid @RequestBody ContactReportDto report) {
		return contactReportService.submitReportData(report);
	}

//	@PostMapping(value ="/updateReport")
//	public String updateReportData(@Valid @RequestBody ContactReportInfoDto report) {
//		return contactReportService.updateDraftReport(report);
//	}

	@PostMapping(value = "/deleteReportById")
	public void deleteReportById(@RequestBody long contactReportId) {
		contactReportService.deleteReportById(contactReportId);
	}

	@GetMapping(value = "/getReportById/{contactReportId}")
	public ContactReportDto getReportById(@PathVariable long contactReportId) {
		return contactReportService.findByContactReportId(contactReportId);
	}

	@GetMapping(value = "/getReportsByUserID/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, List<ContactReportInfo>> getMyContactReport(@PathVariable("userId") String userId) {
		return contactReportService.getMyContactReport(userId, (contactReportInfos,status) -> contactReportInfos.stream().filter(c -> c.getContactStatus() == status).collect(Collectors.toList()));
	}

}
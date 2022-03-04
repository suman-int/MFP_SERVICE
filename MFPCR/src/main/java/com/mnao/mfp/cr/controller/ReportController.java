package com.mnao.mfp.cr.controller;

import com.mnao.mfp.cr.Service.ContactReportServiceImpl;
import com.mnao.mfp.cr.Service.GenericResponseWrapper;
import com.mnao.mfp.cr.dto.ContactReportDto;
import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.util.ContactReportEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/ContactReport")
public class ReportController {

    @Autowired(required = true)
    private ContactReportServiceImpl contactReportService;


    @PostMapping(value = "/submitReport")
    public ContactReportResponse submitReportData(@Valid @RequestBody ContactReportDto report) {
        try {
            return GenericResponseWrapper.contactReportResponseFunction.apply(contactReportService.submitReportData(report), null);
        } catch (Exception e) {
            return GenericResponseWrapper.contactReportResponseFunction.apply(null, e.getMessage());
        }
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
    public ContactReportResponse getMyContactReport(@PathVariable("userId") String userId) {

        try {
            return GenericResponseWrapper.contactReportResponseFunction.apply(contactReportService.getMyContactReport(userId, (contactReportInfos, status) -> contactReportInfos.stream().filter(c -> c.getContactStatus() == status).collect(Collectors.toList())), null);

        } catch (Exception e) {
            return GenericResponseWrapper.contactReportResponseFunction.apply(null, e.getMessage());
        }
    }

}
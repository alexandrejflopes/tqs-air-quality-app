package com.example.airquality.controller;

import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.Report;
import com.example.airquality.service.ReportService;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URISyntaxException;

@Controller
public class ReportController {

    ReportService reportService;

    public ReportController(ReportService reportService){
        this.reportService = reportService;
        reportService.setHttpClient(new ReportHttpClient());
    }


    @GetMapping(value = "/")
    public String backToSearch(Model model, @RequestParam(value = "location", required = false) String location) throws ParseException, IOException, URISyntaxException {
        Report report = reportService.getReportForInput(location);
        model.addAttribute("report",report);
        return "index";
    }



}

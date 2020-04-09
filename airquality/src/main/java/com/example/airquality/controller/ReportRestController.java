package com.example.airquality.controller;


import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.Report;
import com.example.airquality.service.ReportService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class ReportRestController {

    @Autowired
    ReportService reportService;

    //@GetMapping(path="/location/{location}", produces = "application/json")
    @GetMapping("/location/{location}")
    public Report getReportForLocation(@PathVariable(name = "location") String location) throws ParseException, IOException, URISyntaxException {
        reportService.setHttpClient(new ReportHttpClient());
        return reportService.getReportForInput(location);
    }



}

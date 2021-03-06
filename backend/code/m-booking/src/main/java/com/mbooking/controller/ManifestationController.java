package com.mbooking.controller;

import com.mbooking.dto.ManifestationDTO;
import com.mbooking.dto.ManifestationImageDTO;
import com.mbooking.dto.ResultsDTO;
import com.mbooking.dto.reports.ReportDTO;
import com.mbooking.service.ManifestationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/manifestation")
public class ManifestationController {

    @Autowired
    ManifestationService manifestSvc;

    @GetMapping
    public ResponseEntity<List<ManifestationDTO>> getAllManifestations(@RequestParam(defaultValue = "0") int pageNum,
                                                                    @RequestParam(defaultValue = "4") int pageSize) {
        return new ResponseEntity<>(manifestSvc.findAll(pageNum, pageSize), HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<ManifestationDTO> getManifestationById(@PathVariable(value="id")Long id) {
        return new ResponseEntity<>(manifestSvc.getManifestationById(id), HttpStatus.OK);
    }

    @GetMapping(value="/search")
    public ResponseEntity<ResultsDTO<ManifestationDTO>> searchManifestations(
            @RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "") String locationName, @RequestParam(defaultValue = "") String date,
            @RequestParam(defaultValue = "0") int pageNum, @RequestParam(defaultValue = "4") int pageSize) {
        return new ResponseEntity<>(
                manifestSvc.searchManifestations(name, type, locationName, date, pageNum, pageSize),
                HttpStatus.OK);
    }

    @PostMapping
    @Secured({ "ROLE_SYS_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ManifestationDTO> createNewManifestation(@Valid @RequestBody ManifestationDTO newManifestData) {

        return new ResponseEntity<>(manifestSvc.createManifestation(newManifestData), HttpStatus.CREATED);
    }


    @PostMapping("/upload/{id}")
    public ResponseEntity<List<ManifestationImageDTO>> uploadManifestationImages(@PathVariable("id")
                                                                                       Long id,
                                                                           @RequestParam("manifestation-images")
                                                                                  MultipartFile[] files) {

        return new ResponseEntity<>(manifestSvc.uploadImages(files, id), HttpStatus.ACCEPTED);
    }

    @PutMapping
    @Secured({"ROLE_SYS_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ManifestationDTO> updateManifestation(@Valid @RequestBody ManifestationDTO manifestData) {
        return new ResponseEntity<>(manifestSvc.updateManifestation(manifestData), HttpStatus.OK);
    }

    @GetMapping("/reports/{id}")
    @Secured({"ROLE_SYS_ADMIN"})
    public ResponseEntity<ReportDTO> reports(@PathVariable Long id) {
        return new ResponseEntity<>(manifestSvc.reports(id), HttpStatus.OK);
    }

}

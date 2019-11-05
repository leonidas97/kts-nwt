package com.mbooking.service.impl;

import com.mbooking.dto.ManifestationDTO;
import com.mbooking.dto.ManifestationSectionDTO;
import com.mbooking.exception.ApiException;
import com.mbooking.model.Manifestation;
import com.mbooking.model.ManifestationDay;
import com.mbooking.model.ManifestationSection;
import com.mbooking.model.Section;
import com.mbooking.repository.ManifestationRepository;
import com.mbooking.service.ConversionService;
import com.mbooking.service.ManifestationService;
import com.mbooking.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ManifestationServiceImpl implements ManifestationService {

    @Autowired
    ManifestationRepository manifestRepo;

    @Autowired
    SectionService sectionSvc;

    @Autowired
    ConversionService conversionSvc;


    /*****************
     Service methods *
     *****************/

    public Manifestation createManifestation(ManifestationDTO newManifestData) {

        Manifestation newManifest = new Manifestation(newManifestData);

        //adding days
        newManifest.setManifestationDays(createManifestDays(newManifestData.getStartDate(),
                newManifestData.getEndDate(), newManifest));

        //adding pictures
        newManifest.setPictures(conversionSvc.convertListToSet(newManifestData.getImages()));

        //adding selected sections
        newManifest.setSelectedSections(createManifestationSections(newManifestData.getSelectedSections(),
                newManifest));

        return save(newManifest);
    }


    /*****************
    Auxiliary methods*
     *****************/

    private Set<ManifestationSection> createManifestationSections(List<ManifestationSectionDTO> sections,
                                                                  Manifestation newManifest) throws ApiException {

        Set<ManifestationSection> selectedSections = new HashSet<>();
        Section section; //section to find

        for(ManifestationSectionDTO sectionDTO: sections) {

            section = sectionSvc.
                    findById(sectionDTO.getSectionID()).
                    orElseThrow(() -> new ApiException("Section not found", HttpStatus.NOT_FOUND));
            selectedSections.add(new ManifestationSection(sectionDTO, section, newManifest));
        }

        return selectedSections;
    }


    private Set<ManifestationDay> createManifestDays(Date start, Date end, Manifestation newManifest) {

        Set<ManifestationDay> manifestDays = new HashSet<ManifestationDay>();
        long numOfDays = getDifferenceDays(start, end);

        Calendar calendar = Calendar.getInstance(); //used to memorize dates between start and end date
        calendar.setTime(start);

        for(int i = 0; i < numOfDays; i++) {
            manifestDays.add(new ManifestationDay(newManifest, calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1); //increment the date
        }


        return manifestDays;
    }

    private long getDifferenceDays(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();

        long hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        //if the manifestation ends on the same day as it started
        // or if it exceeded the following day for more than 12 hours
        if(days == 0 || hours % 24 >= 12) {
            return days + 1; //add an additional day
        } else {
            return days;
        }
        
    }



    /**********************************
     Repository method implementations *
     *********************************/

    public Manifestation save(Manifestation manifestation) {
        return manifestRepo.save(manifestation);
    }

}
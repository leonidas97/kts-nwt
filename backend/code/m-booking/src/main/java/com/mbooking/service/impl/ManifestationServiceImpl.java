package com.mbooking.service.impl;

import com.mbooking.dto.ManifestationDTO;
import com.mbooking.dto.ManifestationSectionDTO;
import com.mbooking.exception.ApiException;
import com.mbooking.model.*;
import com.mbooking.repository.*;
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
    ManifestationDayRepository manifestDayRepo;

    @Autowired
    ReservationRepository reservationRepo;

    @Autowired
    ManifestationSectionRepository manifestSectionRepo;

    @Autowired
    LocationRepository locationRepo;

    @Autowired
    SectionService sectionSvc;

    @Autowired
    ConversionService conversionSvc;


    /*****************
     Service methods *
     *****************/

    public Manifestation createManifestation(ManifestationDTO newManifestData) {

        //check if there is already a manifestation on the specified location and date
        if(checkManifestDateAndLocation(newManifestData, false)) {
            throw new ApiException("Can't have more than one manifestation in the same location at the same time",
                    HttpStatus.CONFLICT);
        }

        Manifestation newManifest = new Manifestation(newManifestData);

        //adding days
        newManifest.setManifestationDays(createManifestDays(newManifestData.getStartDate(),
                newManifestData.getEndDate(), newManifest));

        //adding pictures
        newManifest.setPictures(conversionSvc.convertListToSet(newManifestData.getImages()));

        //adding selected sections
        newManifest.setSelectedSections(createManifestationSections(newManifestData.getSelectedSections(),
                newManifest));

        //adding the location
        Location location = locationRepo.findById(newManifestData.getLocationId()).
                orElseThrow(() -> new ApiException("Location not found", HttpStatus.NOT_FOUND));
        newManifest.setLocation(location);

        return save(newManifest);
    }


    public Manifestation updateManifestation(ManifestationDTO manifestData) {

        if(manifestData.getManifestationId() == null) {
            return null;
        }

        Manifestation manifestToUpdate= findOneById(manifestData.getManifestationId()).
                orElseThrow(() -> new ApiException("Manifestation not found", HttpStatus.NOT_FOUND));


        if(areThereReservations(manifestToUpdate.getId())) {
            throw new ApiException("Can't alter a manifestation with reservations", HttpStatus.UNAUTHORIZED);
        }

        if(checkManifestDateAndLocation(manifestData, true)) {
            throw new ApiException("Can't have more than one manifestation in the same location at the same time",
                    HttpStatus.CONFLICT);
        }

        //updating data
        manifestToUpdate.setName(manifestData.getName());
        manifestToUpdate.setDescription(manifestData.getDescription());
        manifestToUpdate.setManifestationType(manifestData.getType());
        manifestToUpdate.setMaxReservations(manifestData.getMaxReservations());
        manifestToUpdate.setReservableUntil(manifestData.getReservableUntil());
        manifestToUpdate.setReservationsAvailable(manifestData.isReservationsAllowed());
        manifestToUpdate.setPictures(conversionSvc.convertListToSet(manifestData.getImages()));

        deleteOldManifestDays(manifestToUpdate);
        deleteOldManifestSections(manifestToUpdate);

        //updating manifestation days
        manifestToUpdate.setManifestationDays(createManifestDays(manifestData.getStartDate(),
                manifestData.getEndDate(), manifestToUpdate));

        //updating location
        Location location = locationRepo.findById(manifestData.getLocationId()).
                orElseThrow(() -> new ApiException("Location not found", HttpStatus.NOT_FOUND));
        manifestToUpdate.setLocation(location);

        //updating selected sections
        manifestToUpdate.setSelectedSections(createManifestationSections(manifestData.getSelectedSections(),
                manifestToUpdate));

        return save(manifestToUpdate);

    }


    /*****************
    Auxiliary methods*
     *****************/

    private boolean checkManifestDateAndLocation(ManifestationDTO manifestData, boolean updating) {

        for(Manifestation manifest: manifestRepo.findByLocationId(manifestData.getLocationId())) {

            for(ManifestationDay manifDay: manifest.getManifestationDays()) {

                //if the dates intersect or dates are equal
                if(!manifDay.getDate().before(manifestData.getStartDate())
                        && !manifDay.getDate().after(manifestData.getEndDate())) {

                    //when updating, the user may leave the same start and end date
                    if(updating && manifest.getId().equals(manifestData.getManifestationId())) {
                        continue;
                    }

                    return true;

                }

            }

        }

        return false;
    }

    
    private boolean areThereReservations(Long manifestationId) {

        for(Reservation reserv: reservationRepo.findAll()) {
            for(ManifestationDay md: reserv.getManifestationDays()) {
                if(md.getManifestation().getId() == manifestationId) {
                    return true;
                }
            }
        }

        return false;

    }

    private void deleteOldManifestDays(Manifestation manifestToUpdate) {

        for(ManifestationDay oldManifestDay: manifestToUpdate.getManifestationDays()) {
            oldManifestDay.setManifestation(null);
            manifestDayRepo.deleteById(oldManifestDay.getId());
        }

       // manifestDayRepo.deleteAll(manifestToUpdate.getManifestationDays());

    }

    private void deleteOldManifestSections(Manifestation manifestToUpdate) {

        for(ManifestationSection oldManifestSection: manifestToUpdate.getSelectedSections()) {
            oldManifestSection.setManifestation(null);
            oldManifestSection.setSelectedSection(null);
            manifestSectionRepo.deleteById(oldManifestSection.getId());
        }

        //manifestSectionRepo.deleteAll(manifestToUpdate.getSelectedSections());
    }

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


    private List<ManifestationDay> createManifestDays(Date start, Date end, Manifestation newManifest) {

        List<ManifestationDay> manifestDays = new ArrayList<ManifestationDay>();
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

    public Optional<Manifestation> findOneById(Long id) {
        return manifestRepo.findById(id);
    }

}

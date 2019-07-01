package lab.cbadenes.hiv.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PatientActive {

    private static final Logger LOG = LoggerFactory.getLogger(PatientActive.class);

    String id;

    String age;

    String ageRange;

    String gender;

    String hivActives;

    String receipActives;

    String polypharmacy;

    public PatientActive(String raw) {
        String[] values = raw.split(";");

        id  = values[0];
        age = values[1];
        ageRange = values[2];
        gender = values[3];
        hivActives = values[4];
        receipActives = values[5];

        polypharmacy = Integer.valueOf(receipActives) >= 5? "1" : "0";

    }

    public String getId() {
        return id;
    }

    public String getAge() {
        return age;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public String getGender() {
        return gender;
    }

    public String getHivActives() {
        return hivActives;
    }

    public String getReceipActives() {
        return receipActives;
    }

    public String getPolypharmacy() {
        return polypharmacy;
    }
}

package lab.cbadenes.hiv.model;

import java.util.Collections;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class InteractionQuery {
    /**
     * "query": {
     "primary_atc_codes": [
     "J05AF06"
     ],
     "co_medication_atc_codes": [
     "S01EC01"
     ],
     "primary_to_primary": "0"
     */

    private List<String> primary_atc_codes = Collections.emptyList();

    private List<String> co_medication_atc_codes = Collections.emptyList();

    private String primary_to_primary = "0";


    public List<String> getPrimary_atc_codes() {
        return primary_atc_codes;
    }

    public void setPrimary_atc_codes(List<String> primary_atc_codes) {
        this.primary_atc_codes = primary_atc_codes;
    }

    public List<String> getCo_medication_atc_codes() {
        return co_medication_atc_codes;
    }

    public void setCo_medication_atc_codes(List<String> co_medication_atc_codes) {
        this.co_medication_atc_codes = co_medication_atc_codes;
    }

    public String getPrimary_to_primary() {
        return primary_to_primary;
    }

    public void setPrimary_to_primary(String primary_to_primary) {
        this.primary_to_primary = primary_to_primary;
    }

    @Override
    public String toString() {
        return "InteractionQuery{" +
                "primary_atc_codes=" + primary_atc_codes +
                ", co_medication_atc_codes=" + co_medication_atc_codes +
                ", primary_to_primary='" + primary_to_primary + '\'' +
                '}';
    }
}

package lab.cbadenes.hiv.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Interaction {

    private static final Logger LOG = LoggerFactory.getLogger(Interaction.class);

    private String primary;

    private String comedication;

    private String interaction;

    public Interaction() {
    }

    public Interaction(String primary, String comedication, String interaction) {
        this.primary = primary;
        this.comedication = comedication;
        this.interaction = interaction;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getComedication() {
        return comedication;
    }

    public void setComedication(String comedication) {
        this.comedication = comedication;
    }

    public String getInteraction() {
        return interaction;
    }

    public void setInteraction(String interaction) {
        this.interaction = interaction;
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "primary='" + primary + '\'' +
                ", comedication='" + comedication + '\'' +
                ", interaction='" + interaction + '\'' +
                '}';
    }
}

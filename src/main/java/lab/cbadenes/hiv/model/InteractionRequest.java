package lab.cbadenes.hiv.model;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class InteractionRequest {

    private InteractionQuery query;

    public InteractionRequest(InteractionQuery query) {
        this.query = query;
    }

    public InteractionQuery getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "InteractionRequest{" +
                "query=" + query +
                '}';
    }
}

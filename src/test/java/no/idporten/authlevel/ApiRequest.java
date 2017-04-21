package no.idporten.authlevel;


public class ApiRequest {

    public ApiRequest() {

    }

    public ApiRequest(String personidentifikator) {
        this.personidentifikator = personidentifikator;
    }


    private String personidentifikator;

    public String getPersonidentifikator() {
        return personidentifikator;
    }

    public void setPersonidentifikator(String personidentifikator) {
        this.personidentifikator = personidentifikator;
    }
}

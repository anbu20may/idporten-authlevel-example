package no.idporten.authlevel;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    private String personidentifikator;

    @SerializedName("harbruktnivå4")
    private boolean harbruktnivaa4;

    public String getPersonidentifikator() {
        return personidentifikator;
    }

    public void setPersonidentifikator(String personidentifikator) {
        this.personidentifikator = personidentifikator;
    }

    public boolean isHarbruktnivaa4() {
        return harbruktnivaa4;
    }

    public void setHarbruktnivaa4(boolean harbruktnivaa4) {
        this.harbruktnivaa4 = harbruktnivaa4;
    }
}

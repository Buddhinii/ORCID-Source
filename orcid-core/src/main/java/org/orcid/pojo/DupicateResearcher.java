package org.orcid.pojo;

public class DupicateResearcher {

    private String orcid;
    private String email;
    private String givenNames;
    private String familyNames;
    private String institution;
    private String createdDate;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenNames() {
        return givenNames;
    }

    public void setGivenNames(String givenNames) {
        this.givenNames = givenNames;
    }

    public String getFamilyNames() {
        return familyNames;
    }

    public void setFamilyNames(String familynames) {
        this.familyNames = familynames;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

}

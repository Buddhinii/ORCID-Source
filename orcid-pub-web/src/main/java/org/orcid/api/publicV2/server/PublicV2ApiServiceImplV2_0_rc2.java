package org.orcid.api.publicV2.server;

import static org.orcid.core.api.OrcidApiConstants.ACTIVITIES;
import static org.orcid.core.api.OrcidApiConstants.ADDRESS;
import static org.orcid.core.api.OrcidApiConstants.BIOGRAPHY;
import static org.orcid.core.api.OrcidApiConstants.EDUCATION;
import static org.orcid.core.api.OrcidApiConstants.EDUCATION_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.EMAIL;
import static org.orcid.core.api.OrcidApiConstants.EMPLOYMENT;
import static org.orcid.core.api.OrcidApiConstants.EMPLOYMENT_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.EXTERNAL_IDENTIFIERS;
import static org.orcid.core.api.OrcidApiConstants.FUNDING;
import static org.orcid.core.api.OrcidApiConstants.FUNDING_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.KEYWORDS;
import static org.orcid.core.api.OrcidApiConstants.ORCID_JSON;
import static org.orcid.core.api.OrcidApiConstants.ORCID_XML;
import static org.orcid.core.api.OrcidApiConstants.OTHER_NAMES;
import static org.orcid.core.api.OrcidApiConstants.PEER_REVIEW;
import static org.orcid.core.api.OrcidApiConstants.PEER_REVIEW_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.PERSON;
import static org.orcid.core.api.OrcidApiConstants.PERSONAL_DETAILS;
import static org.orcid.core.api.OrcidApiConstants.PUTCODE;
import static org.orcid.core.api.OrcidApiConstants.RECORD;
import static org.orcid.core.api.OrcidApiConstants.RESEARCHER_URLS;
import static org.orcid.core.api.OrcidApiConstants.STATUS_PATH;
import static org.orcid.core.api.OrcidApiConstants.VND_ORCID_JSON;
import static org.orcid.core.api.OrcidApiConstants.VND_ORCID_XML;
import static org.orcid.core.api.OrcidApiConstants.WORK;
import static org.orcid.core.api.OrcidApiConstants.WORK_SUMMARY;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.orcid.api.common.swagger.SwaggerUIBuilder;
import org.orcid.api.publicV2.server.delegator.PublicV2ApiServiceDelegator;
import org.orcid.core.api.OrcidApiConstants;
import org.orcid.jaxb.model.groupid_rc1.GroupIdRecord;
import org.orcid.jaxb.model.message.ScopeConstants;
import org.orcid.jaxb.model.record.summary_rc2.ActivitiesSummary;
import org.orcid.jaxb.model.record.summary_rc2.EducationSummary;
import org.orcid.jaxb.model.record.summary_rc2.EmploymentSummary;
import org.orcid.jaxb.model.record.summary_rc2.FundingSummary;
import org.orcid.jaxb.model.record.summary_rc2.PeerReviewSummary;
import org.orcid.jaxb.model.record.summary_rc2.WorkSummary;
import org.orcid.jaxb.model.record_rc2.Education;
import org.orcid.jaxb.model.record_rc2.Employment;
import org.orcid.jaxb.model.record_rc2.Funding;
import org.orcid.jaxb.model.record_rc2.OtherName;
import org.orcid.jaxb.model.record_rc2.PeerReview;
import org.orcid.jaxb.model.record_rc2.PersonExternalIdentifier;
import org.orcid.jaxb.model.record_rc2.ResearcherUrl;
import org.orcid.jaxb.model.record_rc2.Work;
import org.springframework.beans.factory.annotation.Value;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.ExternalDocs;

/**
 * 
 * @author Angel Montenegro
 * 
 */
@Path("/v2.0_rc2")
public class PublicV2ApiServiceImplV2_0_rc2 {

    protected PublicV2ApiServiceDelegator<Education, Employment, PersonExternalIdentifier, Funding, GroupIdRecord, OtherName, PeerReview, ResearcherUrl, Work> serviceDelegator;

    @Resource
    protected SwaggerUIBuilder swaggerUIBuilder;

    public void setServiceDelegator(
            PublicV2ApiServiceDelegator<Education, Employment, PersonExternalIdentifier, Funding, GroupIdRecord, OtherName, PeerReview, ResearcherUrl, Work> serviceDelegator) {
        this.serviceDelegator = serviceDelegator;
    }

    /**
     * Serves the Swagger UI HTML page
     * 
     * @return a 200 response containing the HTML
     */
    @GET
    @Produces(value = { MediaType.TEXT_HTML })
    @Path("/")
    @Operation(description = "Fetch the HTML swagger UI interface", hidden = true)
    public Response viewSwagger() {
        return swaggerUIBuilder.build();
    }

    @GET
    @Produces(value = { MediaType.TEXT_PLAIN })
    @Path(STATUS_PATH)
    @Operation(description = "Check the server status", response = String.class)
    public Response viewStatusText() {
        return serviceDelegator.viewStatusText();
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(ACTIVITIES)
    @Operation(description = "Fetch all Activities", response = ActivitiesSummary.class)
    public Response viewActivities(@PathParam("orcid") String orcid, @Context HttpServletRequest httpRequest) {
        return serviceDelegator.viewActivities(orcid);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON,
            OrcidApiConstants.APPLICATION_CITEPROC })
    @Path(WORK + PUTCODE)
    @Operation(description = "Fetch a Work", notes = "More notes about this method", response = Work.class)
    public Response viewWork(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode, @Context HttpServletRequest httpRequest) {
        if (OrcidApiConstants.APPLICATION_CITEPROC.equals(httpRequest.getHeader("Accept")))
            return serviceDelegator.viewWorkCitation(orcid, putCode);
        return serviceDelegator.viewWork(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORK_SUMMARY + PUTCODE)
    @Operation(description = "Fetch a Work Summary", response = WorkSummary.class)
    public Response viewWorkSummary(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewWorkSummary(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING + PUTCODE)
    @Operation(description = "Fetch a Funding", response = Funding.class)
    public Response viewFunding(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewFunding(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING_SUMMARY + PUTCODE)
    @Operation(description = "Fetch a Funding Summary", response = FundingSummary.class)
    public Response viewFundingSummary(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewFundingSummary(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION + PUTCODE)
    @Operation(description = "Fetch an Education", response = Education.class)
    public Response viewEducation(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewEducation(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION_SUMMARY + PUTCODE)
    @Operation(description = "Fetch an Education Summary", response = EducationSummary.class)
    public Response viewEducationSummary(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewEducationSummary(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT + PUTCODE)
    @Operation(description = "Fetch an Employment", notes = "Retrive a specific education representation", response = Employment.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Employment found", response = Employment.class),
            @ApiResponse(code = 404, message = "Employment not found") })
    public Response viewEmployment(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewEmployment(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT_SUMMARY + PUTCODE)
    @Operation(description = "Fetch an Employment Summary", response = EmploymentSummary.class)
    public Response viewEmploymentSummary(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewEmploymentSummary(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW + PUTCODE)
    @Operation(description = "Fetch a Peer Review", response = PeerReview.class)
    public Response viewPeerReview(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewPeerReview(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW_SUMMARY + PUTCODE)
    @Operation(description = "Fetch a Peer Review Summary", response = PeerReviewSummary.class)
    public Response viewPeerReviewSummary(@PathParam("orcid") String orcid, @PathParam("putCode") Long putCode) {
        return serviceDelegator.viewPeerReviewSummary(orcid, putCode);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(RESEARCHER_URLS)
    @Operation(description = "Fetch all researcher urls for an ORCID ID")
    public Response viewResearcherUrls(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewResearcherUrls(orcid);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(RESEARCHER_URLS + PUTCODE)
    @Operation(description = "Fetch one researcher url for an ORCID ID")
    public Response viewResearcherUrl(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewResearcherUrl(orcid, Long.valueOf(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMAIL)
    @Operation(description = "Fetch all emails for an ORCID ID")
    public Response viewEmails(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewEmails(orcid);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PERSONAL_DETAILS)
    @Operation(description = "Fetch personal details for an ORCID ID")
    public Response viewPersonalDetails(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewPersonalDetails(orcid);
    }

    @GET
    @Path(OTHER_NAMES)
    @Operation(description = "Fetch Other names")
    public Response viewOtherNames(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewOtherNames(orcid);
    }

    @GET
    @Path(OTHER_NAMES + PUTCODE)
    @Operation(description = "Fetch Other name")
    public Response viewOtherName(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewOtherName(orcid, Long.valueOf(putCode));
    }

    @GET
    @Path(EXTERNAL_IDENTIFIERS)
    @Operation(description = "Fetch external identifiers", authorizations = {
            @Authorization(value = "orcid_two_legs", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewExternalIdentifiers(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewExternalIdentifiers(orcid);
    }

    @GET
    @Path(EXTERNAL_IDENTIFIERS + PUTCODE)
    @Operation(description = "Fetch external identifier")
    public Response viewExternalIdentifier(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewExternalIdentifier(orcid, Long.valueOf(putCode));
    }

    @GET
    @Path(KEYWORDS)
    @Operation(description = "Fetch keywords")
    public Response viewKeywords(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewKeywords(orcid);
    }

    @GET
    @Path(KEYWORDS + PUTCODE)
    @Operation(description = "Fetch keyword")
    public Response viewKeyword(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewKeyword(orcid, Long.valueOf(putCode));
    }
           
    @GET
    @Path(ADDRESS)
    @Operation(description = "Fetch all addresses")
    public Response viewAddresses(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewAddresses(orcid);
    }
            
    @GET
    @Path(ADDRESS + PUTCODE)
    @Operation(description = "Fetch an address")
    public Response viewAddress(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewAddress(orcid, Long.valueOf(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(BIOGRAPHY)
    @Operation(description = "Get biography details")
    public Response viewBiography(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewBiography(orcid);
    }

    @GET
    @Path(PERSON)
    @Operation(description = "Fetch person details")
    public Response viewPerson(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewPerson(orcid);
    }
    
    //Record 
    @GET
    @Path(RECORD)
    @Operation(description = "Fetch record details")
    @ExternalDocs(value = "Record XML Schema", url = "https://raw.githubusercontent.com/ORCID/orcid-model/master/src/main/resources/record_2.0_rc2/record-2.0_rc2.xsd")
    public Response viewRecord(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewRecord(orcid);
    }
}

package org.orcid.api.memberV2.server;

import static org.orcid.core.api.OrcidApiConstants.ACTIVITIES;
import static org.orcid.core.api.OrcidApiConstants.ADDRESS;
import static org.orcid.core.api.OrcidApiConstants.BIOGRAPHY;
import static org.orcid.core.api.OrcidApiConstants.CLIENT_PATH;
import static org.orcid.core.api.OrcidApiConstants.EDUCATION;
import static org.orcid.core.api.OrcidApiConstants.EDUCATIONS;
import static org.orcid.core.api.OrcidApiConstants.EDUCATION_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.EMAIL;
import static org.orcid.core.api.OrcidApiConstants.EMPLOYMENT;
import static org.orcid.core.api.OrcidApiConstants.EMPLOYMENTS;
import static org.orcid.core.api.OrcidApiConstants.EMPLOYMENT_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.ERROR;
import static org.orcid.core.api.OrcidApiConstants.EXTERNAL_IDENTIFIERS;
import static org.orcid.core.api.OrcidApiConstants.FUNDING;
import static org.orcid.core.api.OrcidApiConstants.FUNDINGS;
import static org.orcid.core.api.OrcidApiConstants.FUNDING_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.GROUP_ID_RECORD;
import static org.orcid.core.api.OrcidApiConstants.KEYWORDS;
import static org.orcid.core.api.OrcidApiConstants.ORCID_JSON;
import static org.orcid.core.api.OrcidApiConstants.ORCID_XML;
import static org.orcid.core.api.OrcidApiConstants.OTHER_NAMES;
import static org.orcid.core.api.OrcidApiConstants.PEER_REVIEW;
import static org.orcid.core.api.OrcidApiConstants.PEER_REVIEWS;
import static org.orcid.core.api.OrcidApiConstants.PEER_REVIEW_SUMMARY;
import static org.orcid.core.api.OrcidApiConstants.PERMISSIONS_PATH;
import static org.orcid.core.api.OrcidApiConstants.PERMISSIONS_VIEW_PATH;
import static org.orcid.core.api.OrcidApiConstants.PERSON;
import static org.orcid.core.api.OrcidApiConstants.PERSONAL_DETAILS;
import static org.orcid.core.api.OrcidApiConstants.PUTCODE;
import static org.orcid.core.api.OrcidApiConstants.RECORD;
import static org.orcid.core.api.OrcidApiConstants.RESEARCHER_URLS;
import static org.orcid.core.api.OrcidApiConstants.SEARCH_PATH;
import static org.orcid.core.api.OrcidApiConstants.STATUS_PATH;
import static org.orcid.core.api.OrcidApiConstants.VND_ORCID_JSON;
import static org.orcid.core.api.OrcidApiConstants.VND_ORCID_XML;
import static org.orcid.core.api.OrcidApiConstants.WORK;
import static org.orcid.core.api.OrcidApiConstants.WORKS;
import static org.orcid.core.api.OrcidApiConstants.WORK_SUMMARY;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.orcid.api.common.swagger.SwaggerUIBuilder;
import org.orcid.api.member.MemberApiServiceImplHelper;
import org.orcid.api.memberV2.server.delegator.MemberV2ApiServiceDelegator;
import org.orcid.api.notificationsV2.server.delegator.NotificationsApiServiceDelegator;
import org.orcid.core.exception.OrcidNotificationAlreadyReadException;
import org.orcid.jaxb.model.groupid_rc4.GroupIdRecord;
import org.orcid.jaxb.model.groupid_rc4.GroupIdRecords;
import org.orcid.jaxb.model.message.ScopeConstants;
import org.orcid.jaxb.model.notification.permission_rc4.NotificationPermission;
import org.orcid.jaxb.model.notification_rc4.Notification;
import org.orcid.jaxb.model.record.summary_rc4.ActivitiesSummary;
import org.orcid.jaxb.model.record.summary_rc4.EducationSummary;
import org.orcid.jaxb.model.record.summary_rc4.Educations;
import org.orcid.jaxb.model.record.summary_rc4.EmploymentSummary;
import org.orcid.jaxb.model.record.summary_rc4.Employments;
import org.orcid.jaxb.model.record.summary_rc4.FundingSummary;
import org.orcid.jaxb.model.record.summary_rc4.Fundings;
import org.orcid.jaxb.model.record.summary_rc4.PeerReviewSummary;
import org.orcid.jaxb.model.record.summary_rc4.PeerReviews;
import org.orcid.jaxb.model.record.summary_rc4.WorkSummary;
import org.orcid.jaxb.model.record.summary_rc4.Works;
import org.orcid.jaxb.model.record_rc4.Address;
import org.orcid.jaxb.model.record_rc4.Education;
import org.orcid.jaxb.model.record_rc4.Employment;
import org.orcid.jaxb.model.record_rc4.Funding;
import org.orcid.jaxb.model.record_rc4.Keyword;
import org.orcid.jaxb.model.record_rc4.OtherName;
import org.orcid.jaxb.model.record_rc4.PeerReview;
import org.orcid.jaxb.model.record_rc4.PersonExternalIdentifier;
import org.orcid.jaxb.model.record_rc4.ResearcherUrl;
import org.orcid.jaxb.model.record_rc4.Work;
import org.orcid.jaxb.model.record_rc4.WorkBulk;
import org.springframework.beans.factory.annotation.Value;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.ResponseHeader;


/**
 * 
 * @author Angel Montenegro
 * 
 */
@Path("/v2.0_rc4")
public class MemberV2ApiServiceImplV2_0_rc4 extends MemberApiServiceImplHelper {

    @Context
    private UriInfo uriInfo;

    @Resource
    protected SwaggerUIBuilder swaggerUIBuilder;
    
    protected MemberV2ApiServiceDelegator<Education, Employment, PersonExternalIdentifier, Funding, GroupIdRecord, OtherName, PeerReview, ResearcherUrl, Work, WorkBulk, Address, Keyword> serviceDelegator;

    private NotificationsApiServiceDelegator<NotificationPermission> notificationsServiceDelegator;

    public void setServiceDelegator(
            MemberV2ApiServiceDelegator<Education, Employment, PersonExternalIdentifier, Funding, GroupIdRecord, OtherName, PeerReview, ResearcherUrl, Work, WorkBulk, Address, Keyword> serviceDelegator) {
        this.serviceDelegator = serviceDelegator;
    }
    
    public void setNotificationsServiceDelegator(NotificationsApiServiceDelegator<NotificationPermission> notificationsServiceDelegator) {
        this.notificationsServiceDelegator = notificationsServiceDelegator;
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

    /**
     * Serves the Swagger UI o2c OAuth page
     * 
     * @return a 200 response containing the HTML
     */
    @GET
    @Produces(value = { MediaType.TEXT_HTML })
    @Path("/o2c.html")
    @Operation(description = "Fetch the swagger OAuth component", hidden = true)
    public Response viewSwaggerO2c() {
        return swaggerUIBuilder.buildSwaggerO2CHTML();
    }

    /**
     * @return Plain text message indicating health of service
     */
    @GET
    @Produces(value = { MediaType.TEXT_PLAIN })
    @Path(STATUS_PATH)
    @Operation(description = "Check the server status", hidden = true)
    public Response viewStatusText() {
        return serviceDelegator.viewStatusText();
    }    
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(ACTIVITIES)
    @Operation(description = "Fetch all activities", response = ActivitiesSummary.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    @ExternalDocs(value = "Activities XML Schema", url = "https://raw.githubusercontent.com/ORCID/orcid-model/master/src/main/resources/record_2.0_rc4/activities-2.0_rc4.xsd")
    public Response viewActivities(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewActivities(orcid);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORK + PUTCODE)
    @Operation(description = "Fetch a Work", response = Work.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewWork(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewWork(orcid, getPutCode(putCode));
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORKS)
    @Operation(description = "Fetch all works", response = Works.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewWorks(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewWorks(orcid);
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORK_SUMMARY + PUTCODE)
    @Operation(description = "Fetch a Work Summary", response = WorkSummary.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewWorkSummary(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewWorkSummary(orcid, getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORK)
    @Operation(description = "Create a Work", response = URI.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Work created, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Education resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Work representation", response = String.class),
            @ApiResponse(code = 500, message = "Invalid Work representation that wasn't trapped (bad fuzzy date or you tried to add a put code)", response = String.class) })
    public Response createWork(@PathParam("orcid") String orcid, Work work) {
        return serviceDelegator.createWork(orcid, work);
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORKS)
    @Operation(description = "Create a list of Works", response = URI.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "At least one of the works was created", responseHeaders = @ResponseHeader(name = "Location", description = "The created Education resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Work representation", response = String.class),
            @ApiResponse(code = 500, message = "Invalid Work representation that wasn't trapped (bad fuzzy date or you tried to add a put code)", response = String.class) })
    public Response createWorks(@PathParam("orcid") String orcid, WorkBulk works) {
        return serviceDelegator.createWorks(orcid, works);
    }
    
    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORK + PUTCODE)
    @Operation(description = "Update a Work", response = Work.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Work updated") })
    public Response updateWork(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, Work work) {
        return serviceDelegator.updateWork(orcid, getPutCode(putCode), work);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORK + PUTCODE)
    @Operation(description = "Delete a Work", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Work deleted") })
    public Response deleteWork(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteWork(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING + PUTCODE)
    @Operation(description = "Fetch a Funding", response = Funding.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewFunding(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewFunding(orcid, getPutCode(putCode));
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDINGS)
    @Operation(description = "Fetch all fundings", response = Fundings.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })    
    public Response viewFundings(@PathParam("orcid") String orcid) {        
        return serviceDelegator.viewFundings(orcid);
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING_SUMMARY + PUTCODE)
    @Operation(description = "Fetch a Funding Summary", response = FundingSummary.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewFundingSummary(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewFundingSummary(orcid, getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING)
    @Operation(description = "Create a Funding", response = URI.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Funding created, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Education resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Funding representation", response = String.class),
            @ApiResponse(code = 500, message = "Invalid Funding representation that wasn't trapped (bad fuzzy date or you tried to add a put code)", response = String.class) })
    public Response createFunding(@PathParam("orcid") String orcid, Funding funding) {
        return serviceDelegator.createFunding(orcid, funding);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING + PUTCODE)
    @Operation(description = "Update a Funding", response = Funding.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Funding updated") })
    public Response updateFunding(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, Funding funding) {
        return serviceDelegator.updateFunding(orcid, getPutCode(putCode), funding);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(FUNDING + PUTCODE)
    @Operation(description = "Delete a Funding", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Funding deleted") })
    public Response deleteFunding(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteFunding(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION + PUTCODE)
    @Operation(description = "Fetch an Education", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Education.class),
            @ApiResponse(code = 404, message = "putCode not found", response = String.class),
            @ApiResponse(code = 400, message = "Invalid putCode or ORCID ID", response = String.class) })
    public Response viewEducation(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewEducation(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATIONS)
    @Operation(description = "Fetch all educations", response = Educations.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })    
    public Response viewEducations(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewEducations(orcid);
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION_SUMMARY + PUTCODE)
    @Operation(description = "Fetch an Education summary", response = EducationSummary.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewEducationSummary(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewEducationSummary(orcid, getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION)
    @Operation(description = "Create an Education", response = URI.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Education created, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Education resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Education representation", response = String.class),
            @ApiResponse(code = 500, message = "Invalid Education representation that wasn't trapped (bad fuzzy date or you tried to add a put code)", response = String.class) })
    public Response createEducation(@PathParam("orcid") String orcid, Education education) {
        return serviceDelegator.createEducation(orcid, education);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION + PUTCODE)
    @Operation(description = "Update an Education", response = Education.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Education updated") })
    public Response updateEducation(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, Education education) {
        return serviceDelegator.updateEducation(orcid, getPutCode(putCode), education);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EDUCATION + PUTCODE)
    @Operation(description = "Delete an Education", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Education deleted") })
    public Response deleteEducation(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteAffiliation(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT + PUTCODE)
    @Operation(description = "Fetch an Employment", response = Employment.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewEmployment(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewEmployment(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENTS)
    @Operation(description = "Fetch all employments", response = Employments.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })    
    public Response viewEmployments(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewEmployments(orcid);
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT_SUMMARY + PUTCODE)
    @Operation(description = "Fetch an Employment Summary", response = EmploymentSummary.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewEmploymentSummary(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewEmploymentSummary(orcid, getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT)
    @Operation(description = "Create an Employment", response = URI.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Employment created, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Education resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Employment representation", response = String.class),
            @ApiResponse(code = 500, message = "Invalid Employment representation that wasn't trapped (bad fuzzy date or you tried to add a put code)", response = String.class) })
    public Response createEmployment(@PathParam("orcid") String orcid, Employment employment) {
        return serviceDelegator.createEmployment(orcid, employment);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT + PUTCODE)
    @Operation(description = "Update an Employment", response = Employment.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Employment updated") })
    public Response updateEmployment(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, Employment employment) {
        return serviceDelegator.updateEmployment(orcid, getPutCode(putCode), employment);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMPLOYMENT + PUTCODE)
    @Operation(description = "Delete an Employment", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Employment deleted") })
    public Response deleteEmployment(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteAffiliation(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW + PUTCODE)
    @Operation(description = "Fetch a Peer Review", response = PeerReview.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewPeerReview(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewPeerReview(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEWS)
    @Operation(description = "Fetch all peer reviews", response = PeerReviews.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })    
    public Response viewPeerReviews(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewPeerReviews(orcid);
    }
    
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW_SUMMARY + PUTCODE)
    @Operation(description = "Fetch a Peer Review Summary", response = PeerReviewSummary.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    public Response viewPeerReviewSummary(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewPeerReviewSummary(orcid, getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW)
    @Operation(description = "Create a Peer Review", response = URI.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Peer Review created, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Education resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Peer Review representation", response = String.class),
            @ApiResponse(code = 500, message = "Invalid Peer Review representation that wasn't trapped (bad fuzzy date or you tried to add a put code)", response = String.class) })
    public Response createPeerReview(@PathParam("orcid") String orcid, PeerReview peerReview) {
        return serviceDelegator.createPeerReview(orcid, peerReview);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW + PUTCODE)
    @Operation(description = "Update a Peer Review", response = PeerReview.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Peer Review updated") })
    public Response updatePeerReview(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, PeerReview peerReview) {
        return serviceDelegator.updatePeerReview(orcid, getPutCode(putCode), peerReview);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PEER_REVIEW + PUTCODE)
    @Operation(description = "Delete a Peer Review", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.ACTIVITIES_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Peer Review deleted") })
    public Response deletePeerReview(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deletePeerReview(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(GROUP_ID_RECORD + PUTCODE)
    @Operation(description = "Fetch a Group", response = GroupIdRecord.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.GROUP_ID_RECORD_READ, description = "you need this") }) })
    public Response viewGroupIdRecord(@PathParam("putCode") String putCode) {
        return serviceDelegator.viewGroupIdRecord(getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(GROUP_ID_RECORD)
    @Operation(description = "Create a Group", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.GROUP_ID_RECORD_UPDATE, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Group created, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Group resource", response = URI.class) ),
            @ApiResponse(code = 400, message = "Invalid Group representation", response = String.class) })
    public Response createGroupIdRecord(GroupIdRecord groupIdRecord) {
        return serviceDelegator.createGroupIdRecord(groupIdRecord);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(GROUP_ID_RECORD + PUTCODE)
    @Operation(description = "Update a Group", response = GroupIdRecord.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.GROUP_ID_RECORD_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Peer Review updated") })
    public Response updateGroupIdRecord(@PathParam("putCode") String putCode, GroupIdRecord groupIdRecord) {
        return serviceDelegator.updateGroupIdRecord(groupIdRecord, getPutCode(putCode));
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(GROUP_ID_RECORD + PUTCODE)
    @Operation(description = "Delete a Group", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.GROUP_ID_RECORD_UPDATE, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Group deleted") })
    public Response deleteGroupIdRecord(@PathParam("putCode") String putCode) {
        return serviceDelegator.deleteGroupIdRecord(getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(GROUP_ID_RECORD)
    @Operation(description = "Fetch Groups", response = GroupIdRecords.class, authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.GROUP_ID_RECORD_READ, description = "you need this") }) })
    public Response viewGroupIdRecords(@QueryParam("page-size") String pageSize, @QueryParam("page") String page) {
        return serviceDelegator.viewGroupIdRecords(pageSize, page);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(ERROR)
    @Operation(description = "Fetch the most recent error", response = String.class, hidden = true)
    public Response viewError() {
        throw new RuntimeException("Sample Error", new Exception("Sample Exception"));
    }

    // START NOTIFICATIONS
    // ===================
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PERMISSIONS_PATH)
    @Operation(description = "Fetch all notifications for an ORCID ID", hidden = true, authorizations = {
            @Authorization(value = "orcid_two_legs", scopes = { @AuthorizationScope(scope = ScopeConstants.PREMIUM_NOTIFICATION, description = "you need this") }) })
    public Response viewPermissionNotifications(@PathParam("orcid") String orcid) {
        return notificationsServiceDelegator.findPermissionNotifications(orcid);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PERMISSIONS_VIEW_PATH)
    @Operation(description = "Fetch a notification by id", response = Notification.class, authorizations = {
            @Authorization(value = "orcid_two_legs", scopes = { @AuthorizationScope(scope = ScopeConstants.PREMIUM_NOTIFICATION, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Notification found", response = Notification.class),
            @ApiResponse(code = 404, message = "Notification not found", response = String.class),
            @ApiResponse(code = 401, message = "Access denied, this is not your notification", response = String.class) })
    public Response viewPermissionNotification(@PathParam("orcid") String orcid, @PathParam("id") Long id) {
        return notificationsServiceDelegator.findPermissionNotification(orcid, id);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PERMISSIONS_VIEW_PATH)
    @Consumes()
    @Operation(description = "Archive a notification", response = Notification.class, authorizations = {
            @Authorization(value = "orcid_two_legs", scopes = { @AuthorizationScope(scope = ScopeConstants.PREMIUM_NOTIFICATION, description = "you need this") }) })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Notification archived", response = Notification.class),
            @ApiResponse(code = 404, message = "Notification not found", response = String.class),
            @ApiResponse(code = 401, message = "Access denied, this is not your notification", response = String.class) })
    public Response flagAsArchivedPermissionNotification(@PathParam("orcid") String orcid, @PathParam("id") Long id) throws OrcidNotificationAlreadyReadException {
        return notificationsServiceDelegator.flagNotificationAsArchived(orcid, id);
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PERMISSIONS_PATH)
    @Operation(description = "Add a notification", response = URI.class, authorizations = {
            @Authorization(value = "orcid_two_legs", scopes = { @AuthorizationScope(scope = ScopeConstants.PREMIUM_NOTIFICATION, description = "you need this") }) })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Notification added, see HTTP Location header for URI", responseHeaders = @ResponseHeader(name = "Location", description = "The created Notification resource", response = URI.class) ) })
    public Response addPermissionNotification(@PathParam("orcid") String orcid, NotificationPermission notification) {
        return notificationsServiceDelegator.addPermissionNotification(uriInfo, orcid, notification);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(RESEARCHER_URLS)
    @Operation(description = "Fetch all researcher urls for an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewResearcherUrls(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewResearcherUrls(orcid);
    }

    @GET
    @Path(RESEARCHER_URLS + PUTCODE)
    @Operation(description = "Fetch one researcher url for an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewResearcherUrl(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewResearcherUrl(orcid, getPutCode(putCode));
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(RESEARCHER_URLS)
    @Operation(description = "Add a new researcher url for an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response createResearcherUrl(@PathParam("orcid") String orcid, ResearcherUrl researcherUrl) {
        return serviceDelegator.createResearcherUrl(orcid, researcherUrl);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(RESEARCHER_URLS + PUTCODE)
    @Operation(description = "Edits researcher url for an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response editResearcherUrl(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, ResearcherUrl researcherUrl) {
        return serviceDelegator.updateResearcherUrl(orcid, getPutCode(putCode), researcherUrl);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(RESEARCHER_URLS + PUTCODE)
    @Operation(description = "Delete one researcher url from an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response deleteResearcherUrl(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteResearcherUrl(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EMAIL)
    @Operation(description = "Fetch all emails for an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewEmails(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewEmails(orcid);
    }

    // Other names
    @GET
    @Path(OTHER_NAMES + PUTCODE)
    @Operation(description = "Fetch Other name", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewOtherName(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewOtherName(orcid, getPutCode(putCode));
    }

    @GET
    @Path(OTHER_NAMES)
    @Operation(description = "Fetch Other names", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewOtherNames(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewOtherNames(orcid);
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(OTHER_NAMES)
    @Operation(description = "Add other name", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response createOtherName(@PathParam("orcid") String orcid, OtherName otherName) {
        return serviceDelegator.createOtherName(orcid, otherName);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(OTHER_NAMES + PUTCODE)
    @Operation(description = "Edit other name", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response editOtherName(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, OtherName otherName) {
        return serviceDelegator.updateOtherName(orcid, getPutCode(putCode), otherName);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(OTHER_NAMES + PUTCODE)
    @Operation(description = "Delete other name", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response deleteOtherName(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteOtherName(orcid, getPutCode(putCode));
    }

    // Personal details
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PERSONAL_DETAILS)
    @Operation(description = "Fetch personal details for an ORCID ID", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewPersonalDetails(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewPersonalDetails(orcid);
    }

    // External Identifiers
    @GET
    @Path(EXTERNAL_IDENTIFIERS + PUTCODE)
    @Operation(description = "Fetch external identifier", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewExternalIdentifier(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewExternalIdentifier(orcid, getPutCode(putCode));
    }

    @GET
    @Path(EXTERNAL_IDENTIFIERS)
    @Operation(description = "Fetch external identifiers", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewExternalIdentifiers(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewExternalIdentifiers(orcid);
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EXTERNAL_IDENTIFIERS)
    @Operation(description = "Add external identifier", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response createExternalIdentifier(@PathParam("orcid") String orcid, PersonExternalIdentifier externalIdentifier) {
        return serviceDelegator.createExternalIdentifier(orcid, externalIdentifier);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EXTERNAL_IDENTIFIERS + PUTCODE)
    @Operation(description = "Edit external identifier", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response editExternalIdentifier(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, PersonExternalIdentifier externalIdentifier) {
        return serviceDelegator.updateExternalIdentifier(orcid, getPutCode(putCode), externalIdentifier);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EXTERNAL_IDENTIFIERS + PUTCODE)
    @Operation(description = "Delete external identifier", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response deleteExternalIdentifier(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteExternalIdentifier(orcid, getPutCode(putCode));
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(BIOGRAPHY)
    @Operation(description = "Get biography details", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewBiography(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewBiography(orcid);
    }

    // Keywords
    @GET
    @Path(KEYWORDS + PUTCODE)
    @Operation(description = "Fetch keyword", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewKeyword(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewKeyword(orcid, getPutCode(putCode));
    }

    @GET
    @Path(KEYWORDS)
    @Operation(description = "Fetch keywords", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewKeywords(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewKeywords(orcid);
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(KEYWORDS)
    @Operation(description = "Add keyword", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response createKeyword(@PathParam("orcid") String orcid, Keyword keyword) {
        return serviceDelegator.createKeyword(orcid, keyword);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(KEYWORDS + PUTCODE)
    @Operation(description = "Edit keyword", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response editKeyword(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, Keyword keyword) {
        return serviceDelegator.updateKeyword(orcid, getPutCode(putCode), keyword);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(KEYWORDS + PUTCODE)
    @Operation(description = "Delete keyword", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response deleteKeyword(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteKeyword(orcid, getPutCode(putCode));
    }

    // Address
    @GET
    @Path(ADDRESS + PUTCODE)
    @Operation(description = "Fetch an address", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewAddress(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.viewAddress(orcid, getPutCode(putCode));
    }

    @GET
    @Path(ADDRESS)
    @Operation(description = "Fetch all addresses of a profile", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewAddresses(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewAddresses(orcid);
    }

    @POST
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(ADDRESS)
    @Operation(description = "Add an address", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response createAddress(@PathParam("orcid") String orcid, Address address) {
        return serviceDelegator.createAddress(orcid, address);
    }

    @PUT
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Consumes(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(ADDRESS + PUTCODE)
    @Operation(description = "Edit an address", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response editAddress(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode, Address address) {
        return serviceDelegator.updateAddress(orcid, getPutCode(putCode), address);
    }

    @DELETE
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(ADDRESS + PUTCODE)
    @Operation(description = "Delete an address", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_UPDATE, description = "you need this") }) })
    public Response deleteAddress(@PathParam("orcid") String orcid, @PathParam("putCode") String putCode) {
        return serviceDelegator.deleteAddress(orcid, getPutCode(putCode));
    }

    // Person
    @GET
    @Path(PERSON)
    @Operation(description = "Fetch person details", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.PERSON_READ_LIMITED, description = "you need this") }) })
    public Response viewPerson(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewPerson(orcid);
    }
    
    //Record 
    @GET
    @Path(RECORD)
    @Operation(description = "Fetch record details", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_LIMITED, description = "you need this") }) })
    @ExternalDocs(value = "Record XML Schema", url = "https://raw.githubusercontent.com/ORCID/orcid-model/master/src/main/resources/record_2.0_rc4/record-2.0_rc4.xsd")
    public Response viewRecord(@PathParam("orcid") String orcid) {
        return serviceDelegator.viewRecord(orcid);
    }

    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON})
    @Path(SEARCH_PATH)
    @Operation(description = "Search records", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_PUBLIC, description = "you need this") }) })
    @ExternalDocs(value = "Record XML Schema", url = "https://raw.githubusercontent.com/ORCID/orcid-model/master/src/main/resources/record_2.0_rc4/search-2.0_rc4.xsd")
    public Response searchByQuery(@QueryParam("q") @DefaultValue("") String query, @Context UriInfo uriInfo) {
        Map<String, List<String>> solrParams = uriInfo.getQueryParameters();
        Response xmlQueryResults = serviceDelegator.searchByQuery(solrParams);
        return xmlQueryResults;
    }

    @GET
    @Path(CLIENT_PATH)
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML, VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Operation(description = "Fetch client details", authorizations = {
            @Authorization(value = "orcid_auth", scopes = { @AuthorizationScope(scope = ScopeConstants.READ_PUBLIC, description = "you need this") }) })
    @ExternalDocs(value = "Record XML Schema", url = "https://raw.githubusercontent.com/ORCID/orcid-model/master/src/main/resources/record_2.0_rc4/record-2.0_rc4.xsd")
    public Response viewClient(@PathParam("client_id") String clientId) {
        return serviceDelegator.viewClient(clientId);
    }
}

package org.orcid.api.common.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Resource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.orcid.core.analytics.AnalyticsData;
import org.orcid.core.analytics.client.AnalyticsClient;
import org.orcid.core.api.OrcidApiConstants;
import org.orcid.core.manager.ClientDetailsEntityCacheManager;
import org.orcid.core.manager.EncryptionManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.jaxb.model.clientgroup.ClientType;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.test.OrcidJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OrcidJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-core-context.xml" })
public class AnalyticsProcessTest {

    @Mock
    private AnalyticsClient analyticsClient;

    @Mock
    private ClientDetailsEntityCacheManager clientDetailsEntityCacheManager;

    @Mock
    private ProfileEntityCacheManager profileEntityCacheManager;

    @Mock
    private SecurityContext mockSecurityContext;

    @Mock
    private PropertiesDelegate mockPropertiesDelegate;

    @Resource
    private EncryptionManager encryptionManager;

    private String hashedOrcid;

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        MockitoAnnotations.openMocks(this);
        hashedOrcid = encryptionManager.sha256Hash("1234-4321-1234-4321");
    }

    @Test
    public void testAnalyticsProcessForPublicClient() throws InterruptedException {
        String clientDetailsId = "some-client-details-id";
        Mockito.when(clientDetailsEntityCacheManager.retrieve(Mockito.eq(clientDetailsId))).thenReturn(getPublicClient());
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequest();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setClientDetailsId(clientDetailsId);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("works", data.getCategory());
        assertEquals("Public API v2.0", data.getApiVersion());
        assertEquals(ClientType.PUBLIC_CLIENT.name() + " | a public client - some-client-details-id", data.getClientDetailsString());
        assertEquals("37.14.150.0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertEquals(MediaType.APPLICATION_XML, data.getContentType());
    }

    @Test
    public void testAnalyticsProcessForPublicClientWithAmpersand() throws InterruptedException {
        String clientDetailsId = "some-client-details-id";
        Mockito.when(clientDetailsEntityCacheManager.retrieve(Mockito.eq(clientDetailsId))).thenReturn(getPublicClientWithAmpersand());
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequest();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setClientDetailsId(clientDetailsId);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("works", data.getCategory());
        assertEquals("Public API v2.0", data.getApiVersion());
        assertEquals(ClientType.PUBLIC_CLIENT.name() + " | a public + client - some-client-details-id", data.getClientDetailsString());
        assertEquals("37.14.150.0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertEquals(MediaType.APPLICATION_XML, data.getContentType());
    }

    @Test
    public void testSchemeCorrection() throws InterruptedException {
        String clientDetailsId = "some-client-details-id";
        Mockito.when(clientDetailsEntityCacheManager.retrieve(Mockito.eq(clientDetailsId))).thenReturn(getPublicClient());
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequestWithHttpScheme();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setClientDetailsId(clientDetailsId);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
    }

    @Test
    public void testAnalyticsProcessForIPv6() throws InterruptedException {
        String clientDetailsId = "some-client-details-id";
        Mockito.when(clientDetailsEntityCacheManager.retrieve(Mockito.eq(clientDetailsId))).thenReturn(getPublicClient());
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequest();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setClientDetailsId(clientDetailsId);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("0:0:0:0:0:0:0:1");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("works", data.getCategory());
        assertEquals("Public API v2.0", data.getApiVersion());
        assertEquals(ClientType.PUBLIC_CLIENT.name() + " | a public client - some-client-details-id", data.getClientDetailsString());
        assertEquals("0:0:0:0:0:0:0:0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertEquals(MediaType.APPLICATION_XML, data.getContentType());
    }

    @Test
    public void testAnalyticsProcessForMemberClient() throws InterruptedException {
        String clientDetailsId = "some-client-details-id";
        Mockito.when(clientDetailsEntityCacheManager.retrieve(Mockito.eq(clientDetailsId))).thenReturn(getMemberClient());
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequest();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setClientDetailsId(clientDetailsId);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(false);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("works", data.getCategory());
        assertEquals("Member API v2.0", data.getApiVersion());
        assertEquals(ClientType.CREATOR.name() + " | a member client - some-client-details-id", data.getClientDetailsString());
        assertEquals("37.14.150.0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertEquals(MediaType.APPLICATION_XML, data.getContentType());
    }

    @Test
    public void testAnalyticsProcessForAnonymous() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequest();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("works", data.getCategory());
        assertEquals("Public API v2.0", data.getApiVersion());
        assertEquals("Unknown", data.getClientDetailsString());
        assertEquals("37.14.150.0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertEquals(MediaType.APPLICATION_XML, data.getContentType());
    }

    @Test
    public void testAnalyticsProcessForNoSpecifiedCategory() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequestWithNoCategory();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("record", data.getCategory());
        assertEquals("Public API v2.0", data.getApiVersion());
        assertEquals("Unknown", data.getClientDetailsString());
        assertEquals("37.14.150.0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertEquals(MediaType.APPLICATION_XML, data.getContentType());
    }

    @Test
    public void testAnalyticsProcessWithNoContentType() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getRequestWithNoContentType();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("works", data.getCategory());
        assertEquals("Public API v2.0", data.getApiVersion());
        assertEquals("Unknown", data.getClientDetailsString());
        assertEquals("37.14.150.0", data.getIpAddress());
        assertEquals(Integer.valueOf(200), data.getResponseCode());
        assertEquals("blah", data.getUserAgent());
        assertNotNull(data.getContentType());
        assertEquals("default", data.getContentType()); // default content type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithJsonAcceptHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithJsonAcceptHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals(MediaType.APPLICATION_JSON, data.getContentType()); // default
                                                                         // content
                                                                         // type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithJsonContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithJsonContentTypeHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals(OrcidApiConstants.ORCID_JSON, data.getContentType()); // default
                                                                           // content
                                                                           // type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithXmlAcceptHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithXmlAcceptHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals(MediaType.APPLICATION_XML, data.getContentType()); // default
                                                                        // content
                                                                        // type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithXmlContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithXmlContentTypeHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals(OrcidApiConstants.VND_ORCID_XML, data.getContentType()); // default
                                                                              // content
                                                                              // type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithUnknownAcceptHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithUnknownAcceptHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("GET", data.getMethod());
        assertEquals("something/weird", data.getContentType()); // default
                                                                // content type
    }

    @Test
    public void testAnalyticsProcessForPostRequestWithUnknownContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getPostRequestWithUnknownContentTypeHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals("something/weird", data.getContentType()); // default
                                                                // content type
    }

    @Test
    public void testAnalyticsProcessForPostRequestWithJsonContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getPostRequestWithJsonContentTypeHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals(MediaType.APPLICATION_JSON, data.getContentType()); // default
                                                                         // content
                                                                         // type
    }

    @Test
    public void testAnalyticsProcessForPostRequestWithXmlContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getPostRequestWithXmlContentTypeHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("POST", data.getMethod());
        assertEquals(OrcidApiConstants.ORCID_XML, data.getContentType()); // default
                                                                          // content
                                                                          // type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithRdfXmlContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithRdfXmlAcceptHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("GET", data.getMethod());
        assertEquals(OrcidApiConstants.APPLICATION_RDFXML, data.getContentType()); // default
                                                                                   // content
                                                                                   // type
    }

    @Test
    public void testAnalyticsProcessForGetRequestWithJsonLdContentTypeHeader() throws InterruptedException {
        Mockito.when(profileEntityCacheManager.retrieve(Mockito.eq("1234-4321-1234-4321"))).thenReturn(getProfileEntity());

        ContainerRequest request = getGetRequestWithJsonLdAcceptHeader();
        ContainerResponse response = getResponse(request);

        AnalyticsProcess process = new AnalyticsProcess();
        process.setRequest(request);
        process.setResponse(response);
        process.setAnalyticsClient(analyticsClient);
        process.setClientDetailsEntityCacheManager(clientDetailsEntityCacheManager);
        process.setProfileEntityCacheManager(profileEntityCacheManager);
        process.setPublicApi(true);
        process.setIp("37.14.150.83");
        process.setScheme("https");

        Thread t = new Thread(process);
        t.start();
        t.join();

        ArgumentCaptor<AnalyticsData> captor = ArgumentCaptor.forClass(AnalyticsData.class);
        Mockito.verify(analyticsClient).sendAnalyticsData(captor.capture());

        AnalyticsData data = captor.getValue();
        assertNotNull(data);
        assertEquals("GET", data.getMethod());
        assertEquals(OrcidApiConstants.JSON_LD, data.getContentType()); // default
                                                                        // content
                                                                        // type
    }

    private ClientDetailsEntity getMemberClient() {
        ClientDetailsEntity client = new ClientDetailsEntity();
        client.setClientName("a member client");
        client.setClientType(ClientType.CREATOR.name());
        return client;
    }

    private ClientDetailsEntity getPublicClient() {
        ClientDetailsEntity client = new ClientDetailsEntity();
        client.setClientName("a public client");
        client.setClientType(ClientType.PUBLIC_CLIENT.name());
        return client;
    }

    private ClientDetailsEntity getPublicClientWithAmpersand() {
        ClientDetailsEntity client = new ClientDetailsEntity();
        client.setClientName("a public & client");
        client.setClientType(ClientType.PUBLIC_CLIENT.name());
        return client;
    }

    private ProfileEntity getProfileEntity() {
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setId("1234-4321-1234-4321");
        profileEntity.setHashedOrcid(hashedOrcid);
        return profileEntity;
    }

    private ContainerResponse getResponse(ContainerRequest request) {
        return new ContainerResponse(getRequestWithHttpScheme(), Response.ok().build());        
    }

    private ContainerRequest getRequestWithHttpScheme() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/xml");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getRequest() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/xml");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getRequestWithNoCategory() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/xml");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getRequestWithNoContentType() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithXmlAcceptHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, "application/xml");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithJsonAcceptHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, "application/json");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithXmlContentTypeHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, OrcidApiConstants.VND_ORCID_XML);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithJsonContentTypeHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, OrcidApiConstants.ORCID_JSON);
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getPostRequestWithXmlContentTypeHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, OrcidApiConstants.ORCID_XML);
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getPostRequestWithJsonContentTypeHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getPostRequestWithUnknownContentTypeHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "something/weird");
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, "application/xml");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithUnknownAcceptHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, OrcidApiConstants.VND_ORCID_JSON);
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, "something/weird");
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithRdfXmlAcceptHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, OrcidApiConstants.JSON_LD);
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, OrcidApiConstants.APPLICATION_RDFXML);
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

    private ContainerRequest getGetRequestWithJsonLdAcceptHeader() {
        ContainerRequest containerRequest = new ContainerRequest(URI.create("https://localhost:8443/orcid-api-web/"),
                URI.create("https://localhost:8443/orcid-api-web/v2.0/1234-4321-1234-4321/works"), "POST", mockSecurityContext, mockPropertiesDelegate, null);
        containerRequest.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        containerRequest.getHeaders().add(HttpHeaders.ACCEPT, OrcidApiConstants.JSON_LD);
        containerRequest.getHeaders().add(HttpHeaders.USER_AGENT, "blah");
        return containerRequest;
    }

}

package org.orcid.core.orgs.load.source.impl;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.orcid.core.manager.OrgDisambiguatedManager;
import org.orcid.core.orgs.OrgDisambiguatedSourceType;
import org.orcid.core.orgs.load.io.FileRotator;
import org.orcid.core.orgs.load.io.OrgDataClient;
import org.orcid.core.orgs.load.source.LoadSourceDisabledException;
import org.orcid.core.orgs.load.source.fighshare.api.FigshareCollectionArticleDetails;
import org.orcid.core.orgs.load.source.fighshare.api.FigshareCollectionArticleFile;
import org.orcid.core.orgs.load.source.fighshare.api.FigshareCollectionArticleSummary;
import org.orcid.core.orgs.load.source.fighshare.api.FigshareCollectionTimeline;
import org.orcid.core.orgs.load.source.grid.GridOrgLoadSource;
import org.orcid.jaxb.model.message.Iso3166Country;
import org.orcid.persistence.constants.OrganizationStatus;
import org.orcid.persistence.dao.OrgDisambiguatedDao;
import org.orcid.persistence.dao.OrgDisambiguatedExternalIdentifierDao;
import org.orcid.persistence.jpa.entities.IndexingStatus;
import org.orcid.persistence.jpa.entities.OrgDisambiguatedEntity;
import org.orcid.persistence.jpa.entities.OrgDisambiguatedExternalIdentifierEntity;
import org.springframework.test.util.ReflectionTestUtils;

public class GridOrgLoadSourceTest {

    @Mock
    private OrgDisambiguatedExternalIdentifierDao orgDisambiguatedExternalIdentifierDao;

    @Mock
    private OrgDisambiguatedDao orgDisambiguatedDao;
    
    @Mock
    private OrgDisambiguatedManager orgDisambiguatedManager;
    
    @Mock
    private FileRotator fileRotator;

    @Mock
    private OrgDataClient orgDataClient;
    
    @InjectMocks
    private GridOrgLoadSource gridOrgLoadSource;

    @Before
    public void before() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        
        ReflectionTestUtils.setField(gridOrgLoadSource, "userAgent", "userAgent");
        ReflectionTestUtils.setField(gridOrgLoadSource, "gridFigshareCollectionUrl", "gridFigshareCollectionUrl");
        ReflectionTestUtils.setField(gridOrgLoadSource, "gridFigshareArticleUrl", "gridFigshareArticleUrl/");
        ReflectionTestUtils.setField(gridOrgLoadSource, "enabled", true);
        
        when(orgDataClient.get(Mockito.eq("gridFigshareCollectionUrl"), Mockito.eq("userAgent"), Mockito.any())).thenReturn(Arrays.asList(getFigshareGridCollectionArticleSummary(1, "2019-01-01T07:00:00"), getFigshareGridCollectionArticleSummary(2, "2020-01-01T07:00:00"), getFigshareGridCollectionArticleSummary(3, "2020-06-01T07:00:00")));
        when(orgDataClient.get(Mockito.eq("gridFigshareArticleUrl/1"), Mockito.eq("userAgent"), Mockito.any())).thenReturn(getFigshareGridCollectionArticleDetails(1));
        when(orgDataClient.get(Mockito.eq("gridFigshareArticleUrl/2"), Mockito.eq("userAgent"), Mockito.any())).thenReturn(getFigshareGridCollectionArticleDetails(2));
        when(orgDataClient.get(Mockito.eq("gridFigshareArticleUrl/3"), Mockito.eq("userAgent"), Mockito.any())).thenReturn(getFigshareGridCollectionArticleDetails(3));
        when(orgDataClient.downloadFile(Mockito.eq("downloadUrl/1"), Mockito.anyString())).thenReturn(true);
        when(orgDataClient.downloadFile(Mockito.eq("downloadUrl/2"), Mockito.anyString())).thenReturn(true);
        when(orgDataClient.downloadFile(Mockito.eq("downloadUrl/3"), Mockito.anyString())).thenReturn(true);
        
        Path path = Paths.get(getClass().getClassLoader().getResource("grid/dummy.zip").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "zipFilePath", testFile.getAbsolutePath());
    }
    
    @Test
    public void testGetSourceName() {
        assertEquals("GRID", gridOrgLoadSource.getSourceName());
    }
    
    @Test(expected = LoadSourceDisabledException.class)
    public void testSetDisabled() {
        ReflectionTestUtils.setField(gridOrgLoadSource, "enabled", false);
        gridOrgLoadSource.loadOrgData();
    }
    
    @Test
    public void testDownloadOrgData() throws URISyntaxException {
        // doesn't matter which data file we choose as this test isn't testing data loading
        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_5_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());

        gridOrgLoadSource.downloadOrgData();
        
        verify(fileRotator, Mockito.times(1)).removeFileIfExists(Mockito.eq(testFile.getAbsolutePath()));
        
        // verify collection with identifier 3 (see setUp method) is chosen
        verify(orgDataClient, Mockito.times(1)).get(Mockito.eq("gridFigshareCollectionUrl"), Mockito.eq("userAgent"), Mockito.any());
        verify(orgDataClient, Mockito.never()).get(Mockito.eq("gridFigshareArticleUrl/1"), Mockito.eq("userAgent"), Mockito.any());
        verify(orgDataClient, Mockito.never()).get(Mockito.eq("gridFigshareArticleUrl/2"), Mockito.eq("userAgent"), Mockito.any());
        verify(orgDataClient, Mockito.times(1)).get(Mockito.eq("gridFigshareArticleUrl/3"), Mockito.eq("userAgent"), Mockito.any());
        verify(orgDataClient, Mockito.never()).downloadFile(Mockito.eq("downloadUrl/1"), Mockito.anyString());
        verify(orgDataClient, Mockito.never()).downloadFile(Mockito.eq("downloadUrl/2"), Mockito.anyString());
        verify(orgDataClient, Mockito.times(1)).downloadFile(Mockito.eq("downloadUrl/3"), Mockito.anyString());
    }

    @Test
    public void execute_Stats_Test_1() throws URISyntaxException {
        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_5_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());

        gridOrgLoadSource.loadOrgData();

        ArgumentCaptor<OrgDisambiguatedEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedEntity.class);
        verify(orgDisambiguatedManager, Mockito.times(1)).createOrgDisambiguated(captor.capture());
        OrgDisambiguatedEntity persisted = captor.getValue();
        assertNotEquals(OrganizationStatus.DEPRECATED.name(), persisted.getStatus());
        assertNotEquals(OrganizationStatus.OBSOLETE.name(), persisted.getStatus());

        verify(orgDisambiguatedManager, times(5)).createOrgDisambiguatedExternalIdentifier(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedManager, never()).updateOrgDisambiguated(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
    }

    @Test
    public void execute_Stats_Test_2() throws URISyntaxException {
        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_4_orgs_27_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        ArgumentCaptor<OrgDisambiguatedEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedEntity.class);
        verify(orgDisambiguatedManager, Mockito.times(4)).createOrgDisambiguated(captor.capture());
        for (OrgDisambiguatedEntity persisted : captor.getAllValues()) {
            assertNotEquals(OrganizationStatus.DEPRECATED.name(), persisted.getStatus());
            assertNotEquals(OrganizationStatus.OBSOLETE.name(), persisted.getStatus());
        }

        verify(orgDisambiguatedManager, times(27)).createOrgDisambiguatedExternalIdentifier(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedManager, never()).updateOrgDisambiguated(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
    }

    @Test
    public void execute_Stats_Test_3() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.r.1", OrgDisambiguatedSourceType.GRID.name())).thenReturn(new OrgDisambiguatedEntity());
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.r.2", OrgDisambiguatedSourceType.GRID.name())).thenReturn(new OrgDisambiguatedEntity());
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.o.1", OrgDisambiguatedSourceType.GRID.name())).thenReturn(new OrgDisambiguatedEntity());
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.o.2", OrgDisambiguatedSourceType.GRID.name())).thenReturn(new OrgDisambiguatedEntity());

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_2_deprecated_2_obsoleted_orgs.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, Mockito.never()).persist(Mockito.any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));

        ArgumentCaptor<OrgDisambiguatedEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedEntity.class);
        verify(orgDisambiguatedManager, times(4)).updateOrgDisambiguated(captor.capture());

        int deprecated = 0;
        int obsolete = 0;

        for (OrgDisambiguatedEntity persisted : captor.getAllValues()) {
            if (OrganizationStatus.DEPRECATED.name().equals(persisted.getStatus())) {
                deprecated++;
            } else if (OrganizationStatus.OBSOLETE.name().equals(persisted.getStatus())) {
                obsolete++;
            }
        }
        assertEquals(2L, deprecated);
        assertEquals(2L, obsolete);

        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
    }

    @Test
    public void execute_JustAddOneExeternalIdentifier_Test() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(1L);
                entity.setName("org_1");
                entity.setSourceId("grid.1");
                entity.setCity("City One");
                entity.setCountry(Iso3166Country.US.name());
                entity.setOrgType("type_1");
                entity.setRegion("Alabama");
                entity.setSourceType(OrgDisambiguatedSourceType.GRID.name());
                entity.setUrl("http://link1.com");
                return entity;
            }
        });
        OrgDisambiguatedExternalIdentifierEntity extId = new OrgDisambiguatedExternalIdentifierEntity();
        extId.setPreferred(Boolean.FALSE);
        OrgDisambiguatedExternalIdentifierEntity extIdPreferred = new OrgDisambiguatedExternalIdentifierEntity();
        extIdPreferred.setPreferred(Boolean.TRUE);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ISNI1", "ISNI")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "FUNDREF1", OrgDisambiguatedSourceType.FUNDREF.name())).thenReturn(extIdPreferred);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ORGREF1", "ORGREF")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "WIKIDATA1", "WIKIDATA")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "http://en.wikipedia.org/wiki/org_1", "WIKIPEDIA_URL")).thenReturn(extId);

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_6_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedManager, times(1)).createOrgDisambiguatedExternalIdentifier(any(OrgDisambiguatedExternalIdentifierEntity.class));

        verify(orgDisambiguatedDao, never()).persist(Mockito.any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedManager, times(1)).createOrgDisambiguatedExternalIdentifier(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedDao, never()).merge(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
    }

    @Test
    public void execute_UpdateExistingInstitute_Test() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(1L);
                entity.setName("org_1");
                entity.setSourceId("grid.1");
                entity.setCity("City One");
                entity.setCountry(Iso3166Country.US.name());
                entity.setOrgType("type_1");
                entity.setRegion("Alabama");
                entity.setSourceType(OrgDisambiguatedSourceType.GRID.name());
                entity.setStatus("active");
                entity.setUrl("http://link1.com");
                return entity;
            }
        });
        OrgDisambiguatedExternalIdentifierEntity extId = new OrgDisambiguatedExternalIdentifierEntity();
        extId.setPreferred(Boolean.FALSE);
        OrgDisambiguatedExternalIdentifierEntity extIdPreferred = new OrgDisambiguatedExternalIdentifierEntity();
        extIdPreferred.setPreferred(Boolean.TRUE);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ISNI1", "ISNI")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "FUNDREF1", OrgDisambiguatedSourceType.FUNDREF.name())).thenReturn(extIdPreferred);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ORGREF1", "ORGREF")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "WIKIDATA1", "WIKIDATA")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "http://en.wikipedia.org/wiki/org_1", "WIKIPEDIA_URL")).thenReturn(extId);

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_updated_5_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, never()).persist(Mockito.any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));

        ArgumentCaptor<OrgDisambiguatedEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedEntity.class);
        verify(orgDisambiguatedManager, times(1)).updateOrgDisambiguated(captor.capture());
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));

        OrgDisambiguatedEntity orgToBeUpdated = captor.getValue();
        assertNotEquals(OrganizationStatus.DEPRECATED.name(), orgToBeUpdated.getStatus());
        assertNotEquals(OrganizationStatus.OBSOLETE.name(), orgToBeUpdated.getStatus());
        assertEquals(Iso3166Country.CR.name(), orgToBeUpdated.getCountry());
        assertEquals(Long.valueOf(1), orgToBeUpdated.getId());
        assertEquals("City One Updated", orgToBeUpdated.getCity());
        assertEquals(IndexingStatus.PENDING, orgToBeUpdated.getIndexingStatus());
        assertEquals("org_1_updated", orgToBeUpdated.getName());
        assertEquals("type_1,type_2", orgToBeUpdated.getOrgType());
        assertEquals("San Jose", orgToBeUpdated.getRegion());
        assertEquals("grid.1", orgToBeUpdated.getSourceId());
        assertEquals(OrgDisambiguatedSourceType.GRID.name(), orgToBeUpdated.getSourceType());
        assertEquals("active", orgToBeUpdated.getStatus());
        assertEquals("http://link1.com/updated", orgToBeUpdated.getUrl());
    }

    @Test
    public void execute_NothingToCreateNothingToUpdate_Test() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(1L);
                entity.setName("org_1");
                entity.setSourceId("grid.1");
                entity.setCity("City One");
                entity.setCountry(Iso3166Country.US.name());
                entity.setOrgType("type_1");
                entity.setRegion("Alabama");
                entity.setSourceType(OrgDisambiguatedSourceType.GRID.name());
                entity.setUrl("http://link1.com");
                return entity;
            }
        });
        OrgDisambiguatedExternalIdentifierEntity extId = new OrgDisambiguatedExternalIdentifierEntity();
        extId.setPreferred(Boolean.FALSE);
        OrgDisambiguatedExternalIdentifierEntity extIdPreferred = new OrgDisambiguatedExternalIdentifierEntity();
        extIdPreferred.setPreferred(Boolean.TRUE);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ISNI1", "ISNI")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "FUNDREF1", OrgDisambiguatedSourceType.FUNDREF.name())).thenReturn(extIdPreferred);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ORGREF1", "ORGREF")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "WIKIDATA1", "WIKIDATA")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "http://en.wikipedia.org/wiki/org_1", "WIKIPEDIA_URL")).thenReturn(extId);

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_5_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, never()).persist(Mockito.any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedDao, never()).merge(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
    }

    @Test
    public void execute_DeprecatedObsoleteInstitutes_1_Test() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.o.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(1L);
                entity.setSourceId("grid.o.1");
                return entity;
            }
        });
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.o.2", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(2L);
                entity.setSourceId("grid.o.2");
                return entity;
            }
        });
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.r.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(3L);
                entity.setSourceId("grid.r.1");
                return entity;
            }
        });

        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.r.2", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(4L);
                entity.setSourceId("grid.r.2");
                return entity;
            }
        });

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_2_deprecated_2_obsoleted_orgs.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, never()).persist(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedDao, times(0)).persist(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, times(0)).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));

        ArgumentCaptor<OrgDisambiguatedEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedEntity.class);
        verify(orgDisambiguatedManager, times(4)).updateOrgDisambiguated(captor.capture());

        int obsoleteCount = 0;
        int deprecatedCount = 0;
        List<OrgDisambiguatedEntity> orgsToBeUpdated = captor.getAllValues();
        for (OrgDisambiguatedEntity entity : orgsToBeUpdated) {
            if ("OBSOLETE".equals(entity.getStatus())) {
                if (entity.getId() == 1L) {
                    assertEquals("grid.o.1", entity.getSourceId());
                } else if (entity.getId() == 2L) {
                    assertEquals("grid.o.2", entity.getSourceId());
                } else {
                    fail("Invalid obsolete org id: " + entity.getId());
                }
                obsoleteCount++;
            } else if ("DEPRECATED".equals(entity.getStatus())) {
                if (entity.getId() == 3L) {
                    assertEquals("grid.1", entity.getSourceParentId());
                } else if (entity.getId() == 4L) {
                    assertEquals("grid.2", entity.getSourceParentId());
                } else {
                    fail("Invalid deprecated org id: " + entity.getId());
                }
                deprecatedCount++;
            }
        }
        assertEquals(2, deprecatedCount);
        assertEquals(2, obsoleteCount);
    }

    @Test
    public void execute_DeprecatedObsoleteInstitutes_2_Test() throws URISyntaxException {
        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_2_deprecated_2_obsoleted_orgs.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, never()).merge(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedDao, times(0)).merge(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, times(0)).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));

        ArgumentCaptor<OrgDisambiguatedEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedEntity.class);

        verify(orgDisambiguatedManager, times(4)).createOrgDisambiguated(captor.capture());

        int obsoleteCount = 0;
        int deprecatedCount = 0;
        List<OrgDisambiguatedEntity> orgsToBeUpdated = captor.getAllValues();
        for (OrgDisambiguatedEntity entity : orgsToBeUpdated) {
            if ("OBSOLETE".equals(entity.getStatus())) {
                assertThat(entity.getSourceId(), anyOf(is("grid.o.1"), is("grid.o.2")));
                obsoleteCount++;
            } else if ("DEPRECATED".equals(entity.getStatus())) {
                assertThat(entity.getSourceId(), anyOf(is("grid.r.1"), is("grid.r.2")));
                deprecatedCount++;
            }
        }
        assertEquals(2, deprecatedCount);
        assertEquals(2, obsoleteCount);
    }

    @Test
    public void execute_AddMissingWikipediaExtId_Test() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(1L);
                entity.setName("org_1");
                entity.setSourceId("grid.1");
                entity.setCity("City One");
                entity.setCountry(Iso3166Country.US.name());
                entity.setOrgType("type_1");
                entity.setRegion("Alabama");
                entity.setSourceType(OrgDisambiguatedSourceType.GRID.name());
                entity.setStatus("active");
                entity.setUrl("http://link1.com");
                return entity;
            }
        });
        OrgDisambiguatedExternalIdentifierEntity extId = new OrgDisambiguatedExternalIdentifierEntity();
        extId.setPreferred(false);

        OrgDisambiguatedExternalIdentifierEntity extIdPreferred = new OrgDisambiguatedExternalIdentifierEntity();
        extIdPreferred.setPreferred(true);

        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ISNI1", "ISNI")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "FUNDREF1", OrgDisambiguatedSourceType.FUNDREF.name())).thenReturn(extIdPreferred);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "ORGREF1", "ORGREF")).thenReturn(extId);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "WIKIDATA1", "WIKIDATA")).thenReturn(extId);

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_5_external_identifiers.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, never()).persist(Mockito.any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedManager, times(1)).createOrgDisambiguatedExternalIdentifier(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedDao, never()).merge(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).merge(any(OrgDisambiguatedExternalIdentifierEntity.class));

        ArgumentCaptor<OrgDisambiguatedExternalIdentifierEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedExternalIdentifierEntity.class);

        verify(orgDisambiguatedManager).createOrgDisambiguatedExternalIdentifier(captor.capture());

        OrgDisambiguatedExternalIdentifierEntity orgToBeUpdated = captor.getValue();
        assertEquals("http://en.wikipedia.org/wiki/org_1", orgToBeUpdated.getIdentifier());
        assertEquals("WIKIPEDIA_URL", orgToBeUpdated.getIdentifierType());
        assertEquals(Boolean.TRUE, orgToBeUpdated.getPreferred());
    }

    @Test
    public void execute_UpdatePreferredIndicator_Test() throws URISyntaxException {
        when(orgDisambiguatedDao.findBySourceIdAndSourceType("grid.1", OrgDisambiguatedSourceType.GRID.name())).thenAnswer(new Answer<OrgDisambiguatedEntity>() {
            @Override
            public OrgDisambiguatedEntity answer(InvocationOnMock invocation) throws Throwable {
                OrgDisambiguatedEntity entity = new OrgDisambiguatedEntity();
                entity.setId(1L);
                entity.setName("org_1");
                entity.setSourceId("grid.1");
                entity.setCity("City One");
                entity.setCountry(Iso3166Country.US.name());
                entity.setOrgType("type_1");
                entity.setRegion("Alabama");
                entity.setSourceType(OrgDisambiguatedSourceType.GRID.name());
                entity.setUrl("http://link1.com");
                return entity;
            }
        });
        
        // On DB WIKIDATA1 is preferred, but in the file WIKIDATA2 is the
        // preferred one
        OrgDisambiguatedExternalIdentifierEntity wikidata1 = new OrgDisambiguatedExternalIdentifierEntity();
        wikidata1.setIdentifier("WIKIDATA1");
        wikidata1.setIdentifierType("WIKIDATA");
        wikidata1.setPreferred(Boolean.TRUE);

        OrgDisambiguatedExternalIdentifierEntity wikidata2 = new OrgDisambiguatedExternalIdentifierEntity();
        wikidata2.setIdentifier("WIKIDATA2");
        wikidata2.setIdentifierType("WIKIDATA");
        wikidata2.setPreferred(Boolean.FALSE);

        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "WIKIDATA1", "WIKIDATA")).thenReturn(wikidata1);
        when(orgDisambiguatedExternalIdentifierDao.findByDetails(1L, "WIKIDATA2", "WIKIDATA")).thenReturn(wikidata2);

        Path path = Paths.get(getClass().getClassLoader().getResource("grid/grid_1_org_2_ext_ids_#2_preferred.json").toURI());
        File testFile = path.toFile();
        ReflectionTestUtils.setField(gridOrgLoadSource, "localDataPath", testFile.getAbsolutePath());
        
        gridOrgLoadSource.loadOrgData();

        verify(orgDisambiguatedDao, never()).merge(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedExternalIdentifierDao, never()).persist(any(OrgDisambiguatedExternalIdentifierEntity.class));
        verify(orgDisambiguatedManager, times(2)).updateOrgDisambiguatedExternalIdentifier(any(OrgDisambiguatedExternalIdentifierEntity.class));

        verify(orgDisambiguatedDao, never()).persist(any(OrgDisambiguatedEntity.class));
        verify(orgDisambiguatedDao, never()).merge(any(OrgDisambiguatedEntity.class));

        ArgumentCaptor<OrgDisambiguatedExternalIdentifierEntity> captor = ArgumentCaptor.forClass(OrgDisambiguatedExternalIdentifierEntity.class);
        verify(orgDisambiguatedManager, times(2)).updateOrgDisambiguatedExternalIdentifier(captor.capture());

        List<OrgDisambiguatedExternalIdentifierEntity> extIdsToBeUpdated = captor.getAllValues();

        OrgDisambiguatedExternalIdentifierEntity wikidata1ExtId = extIdsToBeUpdated.get(0);
        assertEquals("WIKIDATA1", wikidata1ExtId.getIdentifier());
        assertEquals("WIKIDATA", wikidata1ExtId.getIdentifierType());
        assertEquals(Boolean.FALSE, wikidata1ExtId.getPreferred());

        OrgDisambiguatedExternalIdentifierEntity wikidata2ExtId = extIdsToBeUpdated.get(1);
        assertEquals("WIKIDATA2", wikidata2ExtId.getIdentifier());
        assertEquals("WIKIDATA", wikidata2ExtId.getIdentifierType());
        assertEquals(Boolean.TRUE, wikidata2ExtId.getPreferred());
    }
    

    private FigshareCollectionArticleSummary getFigshareGridCollectionArticleSummary(int id, String date) {
        FigshareCollectionArticleSummary summary = new FigshareCollectionArticleSummary();
        summary.setId(id);
        FigshareCollectionTimeline timeline = new FigshareCollectionTimeline();
        timeline.setPosted(date);
        summary.setTimeline(timeline);
        return summary;
    }
    
    private FigshareCollectionArticleDetails getFigshareGridCollectionArticleDetails(int identifier) {
        FigshareCollectionArticleDetails details = new FigshareCollectionArticleDetails();
        FigshareCollectionArticleFile file = new FigshareCollectionArticleFile();
        file.setName("grid.zip");
        file.setDownloadUrl("downloadUrl/" + identifier);
        details.setFiles(Arrays.asList(file));
        return details;
    }

}

package org.orcid.utils.jersey.unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.orcid.jaxb.model.record.summary_v2.ActivitiesSummary;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes({"application/xml", "application/json"})
public class V2ActivitiesSummaryBodyReader implements MessageBodyReader<ActivitiesSummary> {

    private final Unmarshaller unmarshaller;
    
    public V2ActivitiesSummaryBodyReader() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ActivitiesSummary.class);
            unmarshaller = jaxbContext.createUnmarshaller();            
        } catch (JAXBException jaxbException) {
            throw new ProcessingException("Error deserializing a " + ActivitiesSummary.class,
                jaxbException);
        }
    }
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ActivitiesSummary.class;
    }

    @Override
    public ActivitiesSummary readFrom(Class<ActivitiesSummary> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {
        try {
            return (ActivitiesSummary) unmarshaller.unmarshal(entityStream);
        } catch (JAXBException jaxbException) {
            throw new ProcessingException("Error deserializing a " + ActivitiesSummary.class,
                jaxbException);
        }
    }

}

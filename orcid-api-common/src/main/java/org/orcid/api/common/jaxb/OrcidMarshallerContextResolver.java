package org.orcid.api.common.jaxb;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.orcid.core.api.OrcidApiConstants;
import org.orcid.jaxb.model.message.ErrorDesc;
import org.orcid.jaxb.model.message.OrcidMessage;

/**
 * orcid-api - Nov 11, 2011 - OrcidMarshallerContextResolver
 * 
 * @author Declan Newman (declan)
 **/
@Provider
@Consumes(value = { OrcidApiConstants.VND_ORCID_XML, OrcidApiConstants.ORCID_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD })
@Produces(value = { OrcidApiConstants.VND_ORCID_XML, OrcidApiConstants.ORCID_XML, MediaType.APPLICATION_XML })
public class OrcidMarshallerContextResolver implements ContextResolver<Marshaller> {
    private static final Logger logger = Logger.getLogger(OrcidMarshallerContextResolver.class);

    private JAXBContext context;

    @Override
    public Marshaller getContext(Class<?> type) {
        try {
            context = JAXBContext.newInstance(type);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            return new OrcidMarshallerWrapper(marshaller);
        } catch (JAXBException e) {
            logger.error("Cannot create new marshaller", e);
            throw new WebApplicationException(getResponse(e));
        }

    }

    private Response getResponse(Throwable e) {
        OrcidMessage entity = new OrcidMessage();
        entity.setErrorDesc(new ErrorDesc(e.getMessage()));
        return Response.serverError().entity(entity).build();
    }

}

/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */

package org.orcid.jaxb.model.message;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "referred-by")
public class ReferredBy extends OrcidIdBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public ReferredBy() {
        super();
    }

    public ReferredBy(String path) {
        super(path);
    }

    public ReferredBy(OrcidIdBase other) {
        super(other);
    }

}

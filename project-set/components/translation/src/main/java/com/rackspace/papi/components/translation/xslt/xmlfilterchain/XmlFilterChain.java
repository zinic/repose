package com.rackspace.papi.components.translation.xslt.xmlfilterchain;

import com.rackspace.papi.components.translation.xslt.XsltException;
import com.rackspace.papi.components.translation.xslt.XsltParameter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.xml.transform.sax.SAXTransformerFactory;

public class XmlFilterChain {

    private final SAXTransformerFactory factory;
    private final List<XmlFilterReference> filters;

    public XmlFilterChain(SAXTransformerFactory factory, List<XmlFilterReference> filters) {
        this.factory = factory;
        this.filters = filters;
    }

    public SAXTransformerFactory getFactory() {
        return factory;
    }

    public List<XmlFilterReference> getFilters() {
        return filters;
    }

    public void executeChain(InputStream in, OutputStream output, List<XsltParameter> inputs) throws XsltException {
        if (in == null || output == null) {
            return;
        }
        new XmlFilterChainExecutor(this).executeChain(in, output, inputs);
    }
}

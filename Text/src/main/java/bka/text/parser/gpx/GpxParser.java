/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.parser.gpx;

import bka.text.parser.sax.*;
import gpx.*;
import java.io.*;
import java.nio.file.*;
import javax.xml.parsers.*;
import org.xml.sax.*;


/**
 * Parses GPX 1.1 documents into immutable {@link Gpx} objects using {@link SaxStackHandler}.
 */
public final class GpxParser {

    public Gpx parse(Path path) throws IOException, SAXException, ParserConfigurationException {
        try (InputStream input = Files.newInputStream(path)) {
            return parse(new InputSource(input));
        }
    }

    public Gpx parse(InputStream input) throws IOException, SAXException, ParserConfigurationException {
        return parse(new InputSource(input));
    }

    public Gpx parse(InputSource source) throws IOException, SAXException, ParserConfigurationException {
        SaxStackHandler handler = new SaxStackHandler(new GpxConverter());
        createParser().parse(source, handler);
        return handler.getRoot();
    }

    private static SAXParser createParser() throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        parser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return parser;
    }

}

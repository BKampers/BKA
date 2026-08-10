package bka.text.parser.sax;

import org.xml.sax.*;

/**
 */
public interface SaxElementConverter {
    
    Object convert(Element element) throws SAXException;
    
}

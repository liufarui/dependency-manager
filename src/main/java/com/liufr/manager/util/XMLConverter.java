package com.liufr.manager.util;

import com.liufr.manager.model.Project;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;

/**
 * @author lfr
 * @date 2020/11/7 0:56
 */
public class XMLConverter {
    public Project convertFromXML(Path path) throws JAXBException, SAXException, ParserConfigurationException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(Project.class);
        Unmarshaller um = context.createUnmarshaller();

        final SAXParserFactory sax = SAXParserFactory.newInstance();
        sax.setNamespaceAware(false);
        final XMLReader reader = sax.newSAXParser().getXMLReader();
        final Source er = new SAXSource(reader, new InputSource(new FileReader(path.toFile())));
        return (Project)um.unmarshal(er);
    }
}

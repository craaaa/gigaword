import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class GigawordParser {


    StringBuilder texts;
    enum Tag{
        DOC,
        HEADLINE,
        DATELINE,
        TEXT,
        P,
        GWENG;
    }


    public String parse(SequenceInputStream file) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        SAXParser saxParser;
        texts = new StringBuilder();
        {
            try {
                saxParser = factory.newSAXParser();

                DefaultHandler handler = new GigawordHandler();
                saxParser.parse(file, handler);
            } catch (ParserConfigurationException e) {
                System.err.println("ParserConfigurationException for file: " + file.toString());
            } catch (SAXException e) {
                System.err.println("SAXException for file: " + file.toString());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("IOException for file: " + file.toString());
            }
        }
        return texts.toString();
    }

    class GigawordHandler extends DefaultHandler {

        private Hashtable notations = new Hashtable();
        private Hashtable entities = new Hashtable();

        boolean isDoc = false;
        boolean isHeadline = false;
        boolean isDateline = false;
        boolean isText = false;
        boolean isPara = false;
        boolean isRoot = false;


        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) throws SAXException {

            switch(Tag.valueOf(qName.toUpperCase())){
                case DOC:
                    isDoc = true;
                    break;
                case HEADLINE:
                    isHeadline = true;
                    break;
                case DATELINE:
                    isDateline = true;
                    break;
                case TEXT:
                    isText = true;
                    break;
                case P:
                    isPara = true;
                    break;
                case GWENG:
                    isRoot = true;
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(Tag.valueOf(qName.toUpperCase())){
                case DOC:
                    isDoc = false;
                    break;
                case HEADLINE:
                    isHeadline = false;
                    break;
                case DATELINE:
                    isDateline = false;
                    break;
                case TEXT:
                    isText = false;
                    break;
                case P:
                    isPara = false;
                    break;
                case GWENG:
                    isRoot = false;
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isHeadline){
                texts.append(new String(ch, start, length).trim());
            }
            else if (isPara){
                texts.append(new String(ch, start, length).trim());
            }
            else if (isText){
                texts.append(new String(ch, start, length).trim());
            }
            else if (isDateline){
            }
        }

        @Override
        public void notationDecl(String name, String publicId, String systemId) throws SAXException {
            System.out.println(name);
            notations.put(name, publicId);
        }


        @Override
        public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
            entities.put(name, publicId);
        }
    }
}
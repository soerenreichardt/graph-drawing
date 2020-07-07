package io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XmlImporterTest {

    @Test
    void shouldParseNodes() {
        XmlImporter xmlImporter = new XmlImporter("/Checkstyle-6.5.graphml", "implementation");
        xmlImporter.parse();
    }
}
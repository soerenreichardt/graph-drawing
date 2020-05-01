package io;

import org.junit.jupiter.api.Test;
import tree.Tree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewickFormatImporterTest {

    @Test
    void shouldParseTokens() {
        NewickFormatImporter importer = new NewickFormatImporter("(foo:1.0,bar)parent:5.0;", true);

        List<NewickFormatImporter.ParsedObject> tokens = importer.tokenize();
        List<Class<? extends NewickFormatImporter.ParsedObject>> expectedTokens = List.of(
                NewickFormatImporter.SubtreeStart.class,
                NewickFormatImporter.NodeName.class,
                NewickFormatImporter.NodeValue.class,
                NewickFormatImporter.SiblingSeparator.class,
                NewickFormatImporter.NodeName.class,
                NewickFormatImporter.SubtreeEnd.class,
                NewickFormatImporter.NodeName.class,
                NewickFormatImporter.NodeValue.class,
                NewickFormatImporter.Stop.class
        );

        for (int i = 0; i < expectedTokens.size(); i++) {
            assertTrue(tokens.get(i).equals(expectedTokens.get(i)));
        }
    }

    @Test
    void shouldImportTree() {
        NewickFormatImporter importer = new NewickFormatImporter("(foo:1.0,bar)parent:5.0;", true);
        Tree<String> tree = importer.parse();

        assertEquals("parent", tree.data());
        assertEquals(2, tree.children().size());
        assertEquals("foo", tree.getLeftChild().data());
        assertEquals("bar", tree.getRightChild().data());
    }

}
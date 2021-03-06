package io;

import org.apache.commons.lang3.tuple.Pair;
import tree.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class NewickFormatImporter {

    private static final char IGNORE_TOKENS = ' ';
    private static final char SUBTREE_START_TOKEN = '(';
    private static final char SUBTREE_END_TOKEN = ')';
    private static final char SIBLING_SEPARATOR = ',';
    private static final char DISTANCE_DELIMITER = ':';
    private static final char STOP_TOKEN = ';';

    private static final List<Character> tokens = Arrays.asList(SUBTREE_START_TOKEN, SUBTREE_END_TOKEN, SIBLING_SEPARATOR, DISTANCE_DELIMITER, STOP_TOKEN);

    String fileContent = "";

    public NewickFormatImporter(String path) {
        String resolvedPath = this.getClass().getResource(path).getPath();
        File treeFile = new File(resolvedPath);
        try {
            Scanner scanner = new Scanner(treeFile);
            while (scanner.hasNextLine()) {
                fileContent += scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
        }
    }

    NewickFormatImporter(String fileContent, boolean testOnly) {
        this.fileContent = fileContent;
    }


    public Tree<String> parse() {
        List<ParsedObject> tokens = filterTokens(tokenize());

        var tokensWithoutDistance = tokens
                .stream()
                .filter(token -> !token.getClass().getSimpleName().equals(NodeValue.class.getSimpleName()))
                .collect(Collectors.toList());

        Stack<List<Tree<String>>> siblings = new Stack<>();
        List<Tree<String>> children = new ArrayList<>();
        Tree<String> currentRoot = new Tree<>("root");
        for(var token : tokensWithoutDistance) {
            if (token.equals(SubtreeStart.class)) {
                siblings.push(new ArrayList<>());
            }
            if (token.equals(SubtreeEnd.class)) {
                children = siblings.pop();
            }
            if (token.equals(NodeName.class)) {
                currentRoot = new Tree<>(((NodeName) token).name);
                if (!siblings.empty()) { // must be root
                    siblings.peek().add(currentRoot);
                }
                if (!children.isEmpty()) {
                    children.forEach(currentRoot::addChild);
                    children.clear();
                }
            }
        }

        return currentRoot;
    }

    List<ParsedObject> tokenize() {
        List<ParsedObject> parsedObjects = new ArrayList<>();

        int position = 0;
        while (position != fileContent.length()) {
            ParsedObject parsedObject;
            char currentChar = fileContent.charAt(position);
            if (Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }
            switch (currentChar) {
                case SUBTREE_START_TOKEN:
                    parsedObject = new SubtreeStart();
                    break;
                case SUBTREE_END_TOKEN:
                    parsedObject = new SubtreeEnd();
                    break;
                case SIBLING_SEPARATOR:
                    parsedObject = new SiblingSeparator();
                    break;
                case DISTANCE_DELIMITER:
                    parsedObject = new NodeValue();
                    maybeAddEmptyNode(parsedObjects);
                    break;
                case STOP_TOKEN:
                    parsedObject = new Stop();
                    maybeAddEmptyNode(parsedObjects);
                    break;
                default:
                    parsedObject = new NodeName();
                    break;
            }

            position = parsedObject.parse(position);
            parsedObjects.add(parsedObject);
            position++;
        }

        return parsedObjects;
    }

    List<ParsedObject> filterTokens(List<ParsedObject> tokens) {
        return tokens
                .stream()
                .filter(parsedObject -> !parsedObject.equals(SiblingSeparator.class) && !parsedObject.equals(Stop.class))
                .collect(Collectors.toList());
    }

    private void maybeAddEmptyNode(List<ParsedObject> parsedObjects) {
        ParsedObject previous = parsedObjects.get(parsedObjects.size() - 1);
        if (!previous.equals(NodeName.class) && !previous.equals(NodeValue.class)) {
            parsedObjects.add(new NodeName());
        }
    }

    private Pair<Integer, String> readSequence(int position) {
        String sequence = "";
        char current;
        while (position < fileContent.length()) {
            current = fileContent.charAt(position);
            if (current == IGNORE_TOKENS) {
                position++;
                continue;
            }

            if (tokens.contains(current)) {
                break;
            }

            sequence += current;
            position++;
        }
        return Pair.of(position, sequence);
    }

    public static abstract class ParsedObject {
        public int parse(int position) {
            return position;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }

        public boolean equals(Class<? extends ParsedObject> c) {
            return getClass().getSimpleName().equals(c.getSimpleName());
        }
    }

    public final class SubtreeStart extends ParsedObject {
    }

    public final class SubtreeEnd extends ParsedObject {
    }

    public final class SiblingSeparator extends ParsedObject {
    }

    public final class Stop extends ParsedObject {
    }

    public final class NodeName extends ParsedObject {

        String name = "";

        @Override
        public int parse(int position) {
            Pair<Integer, String> readResult = readSequence(position);
            this.name = readResult.getRight();
            return readResult.getLeft() - 1;
        }

        @Override
        public String toString() {
            return "NodeName('" + name + "')";
        }
    }

    public final class NodeValue extends ParsedObject {

        String value = "";

        @Override
        public int parse(int position) {
            Pair<Integer, String> readResult = readSequence(position + 1);
            this.value = readResult.getRight();
            return readResult.getLeft() - 1;
        }

        @Override
        public String toString() {
            return "NodeValue('" + value + "')";
        }
    }
}

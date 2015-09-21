package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;

/**
 * Created by olga on 19.09.15.
 */
public class StringSetImpl implements StreamSerializable, StringSet {
    private class Node implements StreamSerializable {
        final static private int ALPHABET_SIZE = 52, ALPHABET_SIZE_IN_BYTE = (ALPHABET_SIZE + 9) / 8;
        private Node[] nextNode = new Node[ALPHABET_SIZE];
        private boolean isTerminal;
        private int numberOfTerminalWithThisPrefix;

        @Override
        public void serialize(OutputStream out) throws SerializationException {
            try {
                BitSet mask = new BitSet(ALPHABET_SIZE_IN_BYTE*8);

                for (int i = 0; i < ALPHABET_SIZE; i++) {
                    if (nextNode[i] != null) {
                        mask.set(i);
                    }
                }

                if (isTerminal) {
                    mask.set(ALPHABET_SIZE_IN_BYTE*8 - 2);
                }

                mask.set(ALPHABET_SIZE_IN_BYTE * 8 - 1);

                out.write(mask.toByteArray());
                out.write(numberOfTerminalWithThisPrefix);
                for (int i = 0; i < ALPHABET_SIZE; i++) {
                    if (nextNode[i] != null) {
                        nextNode[i].serialize(out);
                    }
                }
            } catch (IOException e) {
                throw new SerializationException();
            }
        }

        @Override
        public void deserialize(InputStream in) throws SerializationException {
            try {
                byte[] InputByte = new byte[ALPHABET_SIZE_IN_BYTE];
                BitSet mask = new BitSet(ALPHABET_SIZE_IN_BYTE*8);;
                in.read(InputByte);
                for (int i = 0; i < ALPHABET_SIZE_IN_BYTE*8; i++) {
                    if ((InputByte[i/8] & (1 << (i & 7))) != 0) {
                        mask.set(i);
                    }
                }

                if (mask.get(ALPHABET_SIZE_IN_BYTE*8 - 2) == true) {
                    isTerminal = true;
                }
                numberOfTerminalWithThisPrefix = in.read();
                for (int i = 0; i < ALPHABET_SIZE; ++i) {
                    if (mask.get(i) == true) {
                        nextNode[i] = new Node();
                        nextNode[i].deserialize(in);
                    }
                }
            } catch (IOException e) {
                throw new SerializationException();
            }

        }
    }
    private Node root;

    StringSetImpl() {
        root = new Node();
    }

    private static int charToInt(char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        } else {
            return 26 + c - 'A';
        }
    }
    
    @Override
    public void serialize(OutputStream out) {
        root.serialize(out);
    }

    @Override
    public void deserialize(InputStream in) {
        root.deserialize(in);
    }
    
    private Node GoToTheEndOfElement(String element) {
        Node cur = root;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            if (cur.nextNode[position] == null) {
                return null;
            }
            cur = cur.nextNode[position];
        }
        return cur;
    }
    
    @Override
    public boolean contains(String element) {
        Node cur =  GoToTheEndOfElement(element);
        if (cur == null) {
            return false;
        }

        if (cur.isTerminal) {
            return true;
        }
        return false;
    }

    @Override
    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }

        Node cur = root;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            if (cur.nextNode[position] == null) {
                cur.nextNode[position] = new Node();
            }
            cur = cur.nextNode[position];
        }

        cur.isTerminal = true;
        cur = root;
        cur.numberOfTerminalWithThisPrefix++;
        for (int i = 0; i < element.length(); i++) {
            cur = cur.nextNode[charToInt(element.charAt(i))];
            cur.numberOfTerminalWithThisPrefix++;
        }
        return true;
    }

    @Override
    public boolean remove(String element) {
        Node cur =  GoToTheEndOfElement(element);
        if (cur == null) {
            return false;
        }

        if (cur.isTerminal == false) {
            return false;
        }
        cur.isTerminal = false;
        cur = root;
        cur.numberOfTerminalWithThisPrefix--;
        for (int i = 0; i < element.length(); i++) {
            cur = cur.nextNode[charToInt(element.charAt(i))];
            cur.numberOfTerminalWithThisPrefix--;
        }
        return true;
    }

    @Override
    public int size() {
        return root.numberOfTerminalWithThisPrefix;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node cur =  GoToTheEndOfElement(prefix);
        if (cur == null) {
            return 0;
        }
        return cur.numberOfTerminalWithThisPrefix;
    }
}

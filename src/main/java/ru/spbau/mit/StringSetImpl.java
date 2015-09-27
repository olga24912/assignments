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
        private static final int ALPHABET_SIZE = 52;
        private static final int ALPHABET_SIZE_IN_BYTE = ((ALPHABET_SIZE + 8) / 8)*8;
        private Node[] nextNode = new Node[ALPHABET_SIZE];
        private boolean isTerminal;
        private int numberOfTerminalWithThisPrefix;

        @Override
        public void serialize(OutputStream out) throws SerializationException {
            try {
                BitSet mask = new BitSet(ALPHABET_SIZE_IN_BYTE);

                for (int i = 0; i < ALPHABET_SIZE; i++) {
                    if (nextNode[i] != null) {
                        mask.set(i);
                    }
                }

                mask.set(ALPHABET_SIZE_IN_BYTE - 1, isTerminal);


                byte[] outputByte = new byte[ALPHABET_SIZE_IN_BYTE/8];
                for (int i = 0; i < ALPHABET_SIZE_IN_BYTE; i++) {
                    if (mask.get(i)) {
                        outputByte[i / 8] |= (1 << (i & 7));
                    }
                }

                out.write(outputByte);
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
                byte[] inputByte = new byte[ALPHABET_SIZE_IN_BYTE/8];
                BitSet mask = null;
                in.read(inputByte);
                mask = mask.valueOf(inputByte);

                isTerminal = mask.get(ALPHABET_SIZE_IN_BYTE - 1);

                numberOfTerminalWithThisPrefix = in.read();
                for (int i = 0; i < ALPHABET_SIZE; ++i) {
                    if (mask.get(i)) {
                        nextNode[i] = new Node();
                        nextNode[i].deserialize(in);
                    }
                }
            } catch (IOException e) {
                throw new SerializationException();
            }

        }
    }
    private Node root = new Node();

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

    private Node goToTheEndOfElement(String element, int flag) {
        Node cur = root;
        cur.numberOfTerminalWithThisPrefix += flag;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            if (cur.nextNode[position] == null) {
                return null;
            }
            cur = cur.nextNode[position];
            cur.numberOfTerminalWithThisPrefix += flag;
        }
        return cur;
    }

    @Override
    public boolean contains(String element) {
        Node cur =  goToTheEndOfElement(element, 0);
        return cur == null ? false : cur.isTerminal;
    }

    @Override
    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }

        Node cur = root;
        cur.numberOfTerminalWithThisPrefix++;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            if (cur.nextNode[position] == null) {
                cur.nextNode[position] = new Node();
            }
            cur = cur.nextNode[position];
            cur.numberOfTerminalWithThisPrefix++;
        }

        cur.isTerminal = true;
        return true;
    }

    @Override
    public boolean remove(String element) {
        if (!contains(element)) {
            return false;
        }

        Node cur =  goToTheEndOfElement(element, -1);
        if (cur == null) {
            return false;
        }

        cur.isTerminal = false;
        return true;
    }

    @Override
    public int size() {
        return root.numberOfTerminalWithThisPrefix;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node cur =  goToTheEndOfElement(prefix, 0);
        return cur == null ? 0 : cur.numberOfTerminalWithThisPrefix;
    }
}

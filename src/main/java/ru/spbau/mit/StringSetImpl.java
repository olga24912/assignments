package ru.spbau.mit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by olga on 19.09.15.
 */
public class StringSetImpl implements StreamSerializable, StringSet {
    private class Node implements StreamSerializable{
        final static int ALPHABET_SIZE = 52;
        Node[] nextNode;
        boolean isTerminal;
        int numberOfTerminalWithThisPrefix;

        Node() {
            nextNode = new Node[ALPHABET_SIZE];
        }

        @Override
        public void serialize(OutputStream out) throws SerializationException {
            try {
                byte[] mask = new byte[(ALPHABET_SIZE + 1 + 7)/8];
                for (int i = 0; i < ALPHABET_SIZE; ++i) {
                    if (nextNode[i] != null) {
                        mask[i/8] |= (1 << (i & 7));
                    }
                }

                if (isTerminal) {
                    mask[(ALPHABET_SIZE + 1 + 7) / 8 - 1] |= (1 << 7);
                }
                out.write(mask);
                out.write(numberOfTerminalWithThisPrefix);
                for (int i = 0; i < ALPHABET_SIZE; ++i) {
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
                byte[] mask = new byte[(ALPHABET_SIZE + 1 + 7)/8];
                in.read(mask);
                for (int i = 0; i < ALPHABET_SIZE; ++i) {
                    if (nextNode[i] != null) {
                        mask[i/8] |= (1 << (i & 7));
                    }
                }

                if (((mask[(ALPHABET_SIZE + 1 + 7) / 8 - 1] >> 7) & 1) == 1) {
                    isTerminal = true;
                }
                numberOfTerminalWithThisPrefix = in.read();
                for (int i = 0; i < ALPHABET_SIZE; ++i) {
                    if ((mask[i/8] & (1 << (i & 7))) != 0) {
                        nextNode[i] = new Node();
                        nextNode[i].deserialize(in);
                    }
                }
            } catch (IOException e) {
                throw new SerializationException();
            }

        }
    }

    Node root;

    StringSetImpl() {
        root = new Node();
    }

    private int charToInt(char c) {
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

    @Override
    public boolean add(String element) {
        Node cur = root;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            //System.err.write(position);
            if (cur.nextNode[position] == null) {
                cur.nextNode[position] = new Node();
            }
            cur = cur.nextNode[position];
        }
        if (cur.isTerminal) {
            return false;
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
    public boolean contains(String element) {
        Node cur = root;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            if (cur.nextNode[position] == null) {
                return false;
            }
            cur = cur.nextNode[position];
        }
        if (cur.isTerminal) {
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(String element) {
        Node cur = root;
        for (int i = 0; i < element.length(); i++) {
            int position = charToInt(element.charAt(i));
            if (cur.nextNode[position] == null) {
                return false;
            }
            cur = cur.nextNode[position];
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
        return false;
    }

    @Override
    public int size() {
        return root.numberOfTerminalWithThisPrefix;
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node cur = root;
        for (int i = 0; i < prefix.length(); i++) {
            int position = charToInt(prefix.charAt(i));
            if (cur.nextNode[position] == null) {
                return 0;
            }
            cur = cur.nextNode[position];
        }
        return cur.numberOfTerminalWithThisPrefix;
    }
}

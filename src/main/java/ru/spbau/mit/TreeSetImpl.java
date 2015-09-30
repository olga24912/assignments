package ru.spbau.mit;

import java.util.*;

public class TreeSetImpl<E> extends AbstractSet<E> {
    private final Random rnd = new Random();
    private final Comparator<E> cmp;

    private class Node {
        private final E value;
        private Node leftChild, rightChild;
        private final int y;
        private Node parent;

        public Node(E val) {
            leftChild = null;
            rightChild = null;
            parent = null;
            value = val;
            y = rnd.nextInt();
        }
    }

    private class PairNode {
        private Node r1, r2;

        private PairNode(Node root1, Node root2) {
            r1 = root1;
            r2 = root2;
        }
    }

    private Node root = null;
    private int size = 0;

    private void updateChild(Node r) {
        if (r.leftChild != null) {
            r.leftChild.parent = r;
        }
        if (r.rightChild != null) {
            r.rightChild.parent = r;
        }
        r.parent = null;
    }

    private Node merge(Node r1, Node r2) {
        if (r1 == null) {
            return r2;
        }
        if (r2 == null) {
            return r1;
        }
        if (r1.y < r2.y) {
            r1.rightChild = merge(r1.rightChild, r2);
            updateChild(r1);
            return r1;
        } else {
            r2.leftChild = merge(r1, r2.leftChild);
            updateChild(r2);
            return r2;
        }
    }

    private PairNode split(Node r, E x) {
        if (r == null) return new PairNode(null, null);
        if (cmp.compare(x, r.value) < 0) {
            PairNode splitL = split(r.leftChild, x);
            r.leftChild = splitL.r2;
            updateChild(r);
            return new PairNode(splitL.r1, r);
        } else {
            PairNode splitR = split(r.rightChild, x);
            r.rightChild = splitR.r1;
            updateChild(r);
            return new PairNode(r, splitR.r2);
        }
    }

    private boolean privateContains(Node r, E x) {
        if (r == null) {
            return false;
        } else if (cmp.compare(r.value, x) == 0) {
            return true;
        } else {
            if (cmp.compare(x, r.value) < 0) {
                return privateContains(r.leftChild, x);
            } else {
                return privateContains(r.rightChild, x);
            }
        }
    }

    private Node addNode(Node r, Node x) {
        if (r == null) {
            return x;
        }
        if (x.y < r.y) {
            PairNode newCh = split(r, x.value);
            x.leftChild = newCh.r1;
            x.rightChild = newCh.r2;
            updateChild(x);
            return x;
        } else {
            if (cmp.compare(x.value, r.value) < 0) {
                r.leftChild = addNode(r.leftChild, x);
                updateChild(r);
                return r;
            } else {
                r.rightChild = addNode(r.rightChild, x);
                updateChild(r);
                return r;
            }
        }
    }

    private Node minElement(Node r) {
        if (r == null) {
            return null;
        }
        while (r.leftChild != null) {
            r = r.leftChild;
        }
        return r;
    }

    private Node deleteNode(Node r, E x) {
        if (r == null) {
            return null;
        } else if (cmp.compare(r.value, x) == 0) {
            return merge(r.leftChild, r.rightChild);
        } else if (cmp.compare(x, r.value) < 0) {
            r.leftChild = deleteNode(r.leftChild, x);
            updateChild(r);
            return r;
        } else {
            r.rightChild = deleteNode(r.rightChild, x);
            updateChild(r);
            return r;
        }
    }

    @Override
    public boolean contains(Object x) {
        return privateContains(root, (E) x);
    }

    public boolean add(E x) {
        if (privateContains(root, x)) {
            return false;
        }
        ++size;
        Node currentElement = new Node(x);
        root = addNode(root, currentElement);
        return true;
    }

    @Override
    public boolean remove(Object x) {
        if (!privateContains(root, (E) x)) {
            return false;
        }
        --size;
        root = deleteNode(root, (E) x);
        return true;
    }

    private Node nextElement(Node r) {
        if (r != null && r.rightChild != null) {
            return minElement(r.rightChild);
        }
        while (r != null) {
            if (r.parent != null && r.parent.leftChild == r) {
                return r.parent;
            }
            r = r.parent;
        }
        return null;
    }


    public TreeSetImpl(Comparator<E> comparator) {
        rnd.setSeed(179);
        cmp = comparator;
    }

    private class Iterator implements java.util.Iterator {
        Node nextNode = minElement(root);
        Node currentNode = null;

        @Override
        public boolean hasNext() {
            return !(nextNode == null);
        }

        @Override
        public Object next() throws NoSuchElementException {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            currentNode = nextNode;
            nextNode = nextElement(nextNode);
            return currentNode.value;
        }

        @Override
        public void remove() {
            if (currentNode == null) {
                throw new IllegalStateException();
            }
            root = deleteNode(root, currentNode.value);
            currentNode = null;
        }
    }

    @Override
    public Iterator iterator() {
        return new Iterator();
    }

    @Override
    public int size() {
        return size;
    }
}

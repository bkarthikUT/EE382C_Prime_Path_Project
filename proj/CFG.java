package proj;
import java.util.*;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.Repository;
import org.apache.bcel.generic.*;

public class CFG {
    Set<Node> nodes = new HashSet<Node>();
    Map<Node, Set<Node>> edges = new HashMap<Node, Set<Node>>();

    public static class Node {
        int position;
        Method method;
        JavaClass clazz;

        Node(int p, Method m, JavaClass c) {
            position = p;
            method = m;
            clazz = c;
        }

        public Method getMethod() {
            return method;
        }

        public JavaClass getClazz() {
            return clazz;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node n = (Node)o;
            return (position == n.position) && method.equals(n.method) && clazz.equals(n.clazz);
        }

        public int hashCode() {
            return position + method.hashCode() + clazz.hashCode();
        }

        public String toString() {
            return clazz.getClassName() + '.' + method.getName() + method.getSignature() + ": " + position;
        }

        public String mytoString() {
            return clazz.getClassName() + '.' + method.getName() + ":" + position;
        }
    }

    public void addNode(int p, Method m, JavaClass c) {
        addNode(new Node(p, m, c));
    }

    private void addNode(Node n) {
        nodes.add(n);
        Set<Node> nbrs = edges.get(n);
        if (nbrs == null) {
            nbrs = new HashSet<Node>();
            edges.put(n, nbrs);
        }
    }

    public void addEdge(int p1, Method m1, JavaClass c1, int p2, Method m2, JavaClass c2) {
        Node n1 = new Node(p1, m1, c1);
        Node n2 = new Node(p2, m2, c2);
        addNode(n1);
        addNode(n2);
        Set<Node> nbrs = edges.get(n1);
        nbrs.add(n2);
        edges.put(n1, nbrs);
    }

    public void addEdge(int p1, int p2, Method m, JavaClass c) {
        addEdge(p1, m, c, p2, m, c);
    }

    public String toString() {
        return nodes.size() + " nodes\n" + "nodes: " + nodes + '\n' + "edges: " + edges;
    }

    public void printForProj() {
        System.out.println("num " + nodes.size());
        for(Node n : edges.keySet()){
            for(Node nbr : edges.get(n)){
                System.out.println("e " + n.mytoString() + "," + nbr.mytoString());
            }
        }
    }

    public boolean isReachable(String methodFrom, String clazzFrom,
                               String methodTo, String clazzTo) throws ClassNotFoundException {
        // you will implement this method in Question 2.2
        if (methodFrom == null || clazzFrom == null ||
                methodTo == null || clazzTo == null) throw new IllegalArgumentException();

        InstructionHandle[] s_handles = null, t_handles = null;
        Method s_mtd = null, t_mtd = null;

        JavaClass sjc = Repository.lookupClass(clazzFrom);
        ClassGen scg = new ClassGen(sjc);
        ConstantPoolGen s_cpg = scg.getConstantPool();
        for (Method mtd: scg.getMethods()) {
            String temp = mtd.getName();
            if(mtd.getName().equalsIgnoreCase(methodFrom)) {
                MethodGen smg = new MethodGen(mtd, scg.getClassName(), s_cpg);
                InstructionList sil = smg.getInstructionList();
                s_handles = sil.getInstructionHandles();
                s_mtd = mtd;
                break;
            }
        }

        JavaClass tjc = Repository.lookupClass(clazzTo);
        ClassGen tcg = new ClassGen(tjc);
        ConstantPoolGen t_cpg = tcg.getConstantPool();
        for (Method mtd : tcg.getMethods()) {
            if(mtd.getName().equalsIgnoreCase(methodTo)) {
                MethodGen tmg = new MethodGen(mtd, tcg.getClassName(), t_cpg);
                InstructionList til = tmg.getInstructionList();
                t_handles = til.getInstructionHandles();
                t_mtd = mtd;
                break;
            }
        }

        Set<Node> visited;
        Stack<Node> neighbors;
        boolean exists;
        Node s  = null, t = null;

        for(Node n : nodes){
            if(n.equals(new Node (s_handles[0].getPosition(), s_mtd, sjc))){
                s = n;
            }

            if(n.equals(new Node (t_handles[0].getPosition(), t_mtd, tjc))){
                t = n;
            }
        }

        visited = new HashSet<Node>();
        neighbors = new Stack<Node>();
        neighbors.push(s);

        while(!neighbors.isEmpty()){
            Node n = neighbors.pop();

            if(t.equals(n)) {
                return true;
            }

            for(Node nbr : edges.get(n)){
                if(!visited.contains(nbr)) {
                    visited.add(nbr);
                    neighbors.push(nbr);
                }
            }
        }
        return false;
    }

    public static void main(String args[]) throws ClassNotFoundException {
        GraphGenerator gg = new GraphGenerator();
        CFG res_mtd = gg.createCFGWithMethodInvocation(args[0]);
        res_mtd.printForProj();
    }
}
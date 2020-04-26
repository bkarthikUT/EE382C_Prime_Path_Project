package proj;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrimePath {
    private static Graph graph;
    private static List<String> paths;
    private static HashMap<String, Integer> primePaths;

    public static void main(String []args) {
        if (args.length > 2) {
            System.out.println("Invalid number of args: " + args.length);
            return;
        }
        String graphFile = args[0];
        primePaths = new HashMap<String, Integer>();

        PrimePath.graphInit();
        PrimePath.parseGraphFile(graphFile);
        //PrimePath._printGraph();
        PrimePath.initPaths();
        PrimePath.findPaths();
        //PrimePath._printPaths();
	System.out.println();
	System.out.println("***********************************");
	System.out.println("Prime Paths:");
	System.out.println("------------");
        for (String path : paths) {
            //String savePath = path;
            StringBuffer strbuf = new StringBuffer();
            path = path.replace(" ", "");
            path = path.replace("[", "");
            path = path.replace("]", "");
            String []splits = path.split(",");
            int []pathArr = new int[splits.length];
            for (int x = 0; x < pathArr.length; x++) {
                pathArr[x] = Integer.parseInt(splits[x]);
            }
            if (PrimePath.checkPrime(pathArr)) {
                strbuf.append("[");
                for (int x = 0; x < pathArr.length; x++) {
                    for (Entry<String, Integer> entry : PrimePath.graph.nodeMap.entrySet()) {
                        if (entry.getValue().equals(pathArr[x])) {
                            if(x == pathArr.length - 1){
                                strbuf.append(entry.getKey());
                            }else{
                                strbuf.append(entry.getKey() + ", ");
                            }
                        }
                    }
                }
                strbuf.append("]");
                primePaths.put(new String(strbuf),0);
                System.out.println("--> " + strbuf);
            }
        }

	System.out.println();	
	System.out.println("***********************************");
        if(args.length == 2){
            PrimePath.testPathCoverage(args[1]);
        }
    }

    public static void _printPaths() {
        for (String path : paths) {
            System.out.println(path);
        }
    }

    public static void initPaths() {
        paths = new ArrayList<String>();
    }

    public static void findPaths() {
        // loop through all nodes and do a DPS to find all paths
        for (Node n : graph.getNodeList()) {
            List<Integer> start = new ArrayList<Integer>();
            List<Integer> visited = new ArrayList<Integer>();
            visited.add(n.getId());
            start.add(n.getId());
            PrimePath._dps(start, visited);
        }
    }

    // path will point to the current path we are on
    // visited points to all nodes we've already been to
    public static void _dps(List<Integer> path, List<Integer> visited) {
        int id = path.get(path.size() - 1); // get current id from end of path
        Node n = graph.getNode(id); // current node
        // add path to paths if it doesn't already exist
        if (!paths.contains(path.toString())) {
            paths.add(path.toString());
        }
        if (n.getEdgeList().size() != 0) { // this path has been added so exit since there's nowhere to go
            for (Edge e : n.getEdgeList()) {
                if (!visited.contains(e.getTo())) { // not allowing already visited nodes creates simple paths
                    // mark current as visited, append edge to new path, call _dps, then remove "current" from visited on return
                    visited.add(e.getTo());
                    List<Integer> new_path = new ArrayList<Integer>(path);
                    new_path.add(e.getTo());
                    PrimePath._dps(new_path, visited);
                    visited.remove((Integer)e.getTo());
                }
                else if (e.getTo() == path.get(0)) { // visited but we can end on the start node
                    // add the start node, add the path, remove the start node from the path
                    path.add(e.getTo());
                    paths.add(path.toString());
                    path.remove(path.size() - 1);
                }
            }
        }
    }

    public static boolean checkPrime(int []path) {
        List<Integer> visited = new ArrayList<Integer>();
        for (int id : path) {
            visited.add(id);
        }
        Node start = graph.getNode(path[0]);
        Node end = graph.getNode(path[path.length - 1]);

        // if we have a perfect loop and it's larger than size 1, it can't be extended upon, so prime
        if (start.getId() == end.getId() && path.length > 1) {
            return (true);
        }

        // check if end node can be extended
        for (Edge e : end.getEdgeList()) {
            if (!visited.contains(e.getTo())) { // checks if a node can be added
                return (false);
            }
            else {
                // even though visited contains the node, check if it's the start node
                // if it's a start node, also check there's only one instance
                if (e.getTo() == start.getId()) {
                    int count = 0;
                    // count how many times the start node is in the path
                    for (int x = 0; x < path.length; x++)  {
                        if (path[x] == start.getId()) {
                            count++;
                        }
                    }
                    if (count == 1) { // the node only appears once (the start) so we can end on it, not prime
                        return (false);
                    }
                }
            }
        }

        // check if start node can be extended
        // start node can be extended if there's any other node that contains an edge to
        // start and is not already in visited (unless it's the end, where there will be one instance)
        for (Node n : graph.getNodeList()) {
            boolean extend = false;
            for (Edge e : n.getEdgeList()) {
                if (e.getTo() == start.getId()) {
                    extend = true;
                }
            }
            if (extend) { // we have a node where it has an edge to our start node
                if (!visited.contains(n.getId())) { // if we don't have the node in visited, it can be extended so not prime
                    return (false);
                }
                else {
                    // even though visited contains the node, check if it's the end node
                    // if it's the end node, also check there's only one instance
                    if (n.getId() == end.getId()) {
                        int count = 0;
                        // count how many times the end node is in path
                        for (int x = 0; x < path.length; x++)  {
                            if (path[x] == end.getId()) {
                                count++;
                            }
                        }
                        if (count == 1) { // the node only appears once (the end) so we can prepend on it, not prime
                            return (false);
                        }
                    }
                }
            }
        }

        return (true);
    }

    public static void graphInit() {
        graph = new Graph();
    }

    public static void _printGraph() {
        System.out.println("Graph:");
        System.out.println("    node count: " + graph.getNodeCnt());
        List<Node> nList = graph.getNodeList();
        for (Node n : nList) {
            System.out.println("    Node " + n.getId());
            List<Edge> eList = n.getEdgeList();
            for (Edge e : eList) {
                System.out.println("        edge " + e.getFrom() + " -> " + e.getTo());
            }
        }
    }

    public static void testPathCoverage(String pathFile) {
        BufferedReader reader;
        float count = 0;
        try {
            reader = new BufferedReader(new FileReader(pathFile));
            String line = reader.readLine();

            while (line != null) {
                for (Entry<String, Integer> entry : primePaths.entrySet()) {
                    if(entry.getKey().equals(line)){
                        primePaths.put(entry.getKey(), 1);
                    }
                }
                line = reader.readLine();
            }
            for (Entry<String, Integer> entry : primePaths.entrySet()) {
                if(entry.getValue() == 1){
                    count++;
                }
            }
            System.out.println("Prime path coverage: " + (count/primePaths.size())*100 + "%");
	    System.out.println("***********************************");
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseGraphFile(String graphFile) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(graphFile));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                String []splits = line.split(" ");
                String type = splits[0];
                String value = splits[1];

                if (type.equals("num")) {
                    graph.setNodeCnt(Integer.parseInt(value));
                }
                else if (type.equals("e")) {
                    int from = graph.getNodeNum(value.split(",")[0]);
                    int to = graph.getNodeNum(value.split(",")[1]);
                    List<Node> nList = graph.getNodeList();
                    for (Node n : nList) {
                        if (n.getId() == from) {
                            n.addEdge(to);
                        }
                    }
                }
                else {
                    System.out.println("Bad line:");
                    System.out.println("    type='" + type + "'");
                    System.out.println("    value='" + value + "'");
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Graph {
    private List<Node> nodeList;
    private int nodeCnt;
    public HashMap<String, Integer> nodeMap;

    public Graph() {
        this.nodeCnt = 0;
        this.nodeList = new ArrayList<Node>();
        this.nodeMap = new HashMap<String, Integer>();
    }

    public void setNodeCnt(int nodeCnt) {
        for (int x = 0; x < nodeCnt; x++) {
            this.nodeList.add(new Node(x));
        }
        this.nodeCnt = nodeCnt;
    }

    public int getNodeNum(String node){
        Integer nodeNum = this.nodeMap.get(node);
        if(nodeNum == null){
            nodeNum = this.nodeMap.size();
            this.nodeMap.put(node, nodeNum);
        }
        return (int)(nodeNum);
    }

    public int getNodeCnt() {
        return (this.nodeCnt);
    }

    public List<Node> getNodeList() {
        return (this.nodeList);
    }

    public Node getNode(int id) {
        Node ret = null;
        for (Node n : this.nodeList) {
            if (n.getId() == id) {
                ret = n;
                break;
            }
        }
        return (ret);
    }
}

class Node {
    private int id;
    private List<Edge> edgeList;

    public Node(int id) {
        this.id = id;
        this.edgeList = new ArrayList<Edge>();
    }

    public int getId() {
        return (this.id);
    }

    public void addEdge(int to) {
        this.edgeList.add(new Edge(this.id, to));
    }

    public List<Edge> getEdgeList() {
        return (this.edgeList);
    }
}

class Edge {
    private int from;
    private int to;

    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return (this.from);
    }

    public int getTo() {
        return (this.to);
    }
}

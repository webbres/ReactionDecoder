/*
 * Copyright (c) 2018. BioInception Labs Pvt. Ltd.
 */
package org.openscience.smsd.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Syed Asad Rahman <asad.rahman at bioinceptionlabs.com>
 */
public final class Graph {

    private static final String NEWLINE = System.getProperty("line.separator");

    private final Map<Vertex, Set<Vertex>> adj;
    private final Map<Vertex, Set<Vertex>> c_adj;
    private final Map<Vertex, Set<Vertex>> d_adj;
    private final Map<EdgeType, Set<Edge>> adj_type_Map;
    private final Set<Vertex> vertices;
    private final boolean directed;

    /**
     * Initializes an empty graph with {@code V} vertices and 0 edges.param V
     * the number of vertices
     *
     * @param directed
     */
    public Graph(boolean directed) {
        this.vertices = new HashSet<>();
        this.adj = new TreeMap<>();
        this.c_adj = new TreeMap<>();
        this.d_adj = new TreeMap<>();
        this.adj_type_Map = new HashMap<>();
        this.directed = directed;
    }

    /**
     * Returns the number of vertices in this graph.
     *
     * @return the number of vertices in this graph
     */
    public int V() {
        return vertices.size();
    }

    /**
     * Returns the number of edges in this graph.
     *
     * @return the number of edges in this graph
     */
    public int E() {
        return edges().size();
    }

    /**
     * Returns Nodes in this graph.
     *
     * @return vertices in this graph
     */
    public Set<Vertex> nodes() {
        Set<Vertex> nodes = new HashSet<>();
        nodes.addAll(vertices);
        return nodes;
    }

    /**
     * Returns edges in this graph.
     *
     * @return edges in this graph
     */
    public Set<Edge> edges() {
        Set<Edge> edgesSet = new HashSet<>();
        adj_type_Map.values().forEach((edges) -> {
            edgesSet.addAll(edges);
        });
        return edgesSet;
    }

    private void validateVertex(Vertex v) {
        if (!vertices.contains(v)) {
            throw new IllegalArgumentException("vertex " + v + " not found in the graph");
        }
    }

    /**
     * Adds the undirected edge v-w to this graph. Assumes that the nodes
     * assigned in the edge is already present
     *
     * @param e edge to be added
     */
    public void addEdge(Edge e) {
        validateVertex(e.getSource());
        validateVertex(e.getSink());

        /*
         * Add edges to the map
         */
        addEdge(adj, e);

        /*
         * Add C edges to the map
         */
        if (e.getEdgeType() == EdgeType.C_EDGE) {
            addEdge(c_adj, e);
        }
        /*
         * Add D edges to the map
         */
        if (e.getEdgeType() == EdgeType.D_EDGE) {
            addEdge(d_adj, e);
        }
        /*
         * Add Edge type to the map
         */
        if (!adj_type_Map.containsKey(e.getEdgeType())) {
            adj_type_Map.put(e.getEdgeType(), new HashSet<>());
        }
        adj_type_Map.get(e.getEdgeType()).add(e);

    }

    private void addEdge(Map map, Edge e) {
        addEdge(map, e.getSource(), e.getSink());
        if (!directed) {
            addEdge(map, e.getSink(), e.getSource());
        }
    }

    private void addEdge(Map<Vertex, Set<Vertex>> map, Vertex u, Vertex v) {
        if (!map.containsKey(u)) {
            map.put(u, new HashSet<>());
        }
        map.get(u).add(v);
    }

    /**
     * Adds Vertex to this graph.
     *
     * @param node Vertex to be added
     */
    public void addNode(Vertex node) {
        if (!adj.containsKey(node)) {
            adj.put(node, new HashSet<>());
            vertices.add(node);
        } else {
            throw new IllegalArgumentException("Node " + node + " found in the graph");
        }
    }

    /**
     * Returns the vertices adjacent to vertex {@code v}.
     *
     * @param v the vertex
     * @return the vertices adjacent to vertex {@code v}, as an iterable
     */
    public Set<Vertex> getNeighbours(Vertex v) {
        validateVertex(v);
        return new TreeSet<>(adj.get(v));
    }

    /**
     * Returns the getDegree of vertex {@code v}.
     *
     * @param v the vertex
     * @return the getDegree of vertex {@code v}
     */
    public int getDegree(Vertex v) {
        validateVertex(v);
        return adj.get(v).size();
    }

    /**
     * Returns a string representation of this graph.
     *
     * @return the number of vertices <em>V</em>, followed by the number of
     * edges <em>E</em>, followed by the <em>V</em> adjacency lists
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(vertices.size()).append(" vertices, ").append(edges().size()).append(" edges ").append(NEWLINE);
        adj.entrySet().stream().map((m) -> {
            s.append(m.getKey()).append(": ");
            return m;
        }).map((m) -> {
            m.getValue().forEach((w) -> {
                s.append(w).append(" ");
            });
            return m;
        }).forEachOrdered((_item) -> {
            s.append(NEWLINE);
        });
        return s.toString();
    }

    /**
     * Clean graph
     */
    public void clear() {
        this.vertices.clear();
        this.adj.clear();
        this.c_adj.clear();
        this.d_adj.clear();
        this.adj_type_Map.clear();
    }

    /**
     *
     * @param u
     * @param v
     * @return if an edge exists between vertex
     */
    public boolean hasEdge(Vertex u, Vertex v) {
        return adj.containsKey(u) && adj.get(u).contains(v) ? true
                : !directed && adj.containsKey(v) && adj.get(v).contains(u);
    }

    /**
     * Returns edges of the vertex
     *
     * @param currentVertex
     * @return
     */
    public Iterable<Edge> edgesOf(Vertex currentVertex) {
        Set<Edge> edgesOfVertex = new LinkedHashSet<>();
        edges().stream().map((e) -> {
            if (e.getSource().equals(currentVertex)) {
                edgesOfVertex.add(e);
            }
            return e;
        }).filter((e) -> (!directed && e.getSink().equals(currentVertex))).forEachOrdered((e) -> {
            edgesOfVertex.add(e);
        });
        return edgesOfVertex;
    }

    /**
     * Returns true if there is c edge else false
     *
     * @param u
     * @param v
     * @return true if there is c edge else false
     */
    public boolean isCEdge(Vertex u, Vertex v) {
        validateVertex(u);
        validateVertex(v);
        return c_adj.containsKey(u) && c_adj.get(u).contains(v) ? true
                : !directed && c_adj.containsKey(v) && c_adj.get(v).contains(u);
    }

    /**
     * Returns true if there is d edge else false
     *
     * @param u
     * @param v
     * @return true if there is d edge else false
     */
    public boolean isDEdge(Vertex u, Vertex v) {
        validateVertex(u);
        validateVertex(v);
        return d_adj.containsKey(u) && d_adj.get(u).contains(v) ? true
                : !directed && d_adj.containsKey(v) && d_adj.get(v).contains(u);
    }

    /**
     * Returns an edge connecting source vertex to target vertex if such
     * vertices and such edge exist in this graph. Otherwise returns null. If
     * any of the specified vertices is null returns null In undirected graphs,
     * the returned edge may have its source and target vertices in the opposite
     * order.
     *
     * @param edge
     * @return
     */
    public Vertex getEdgeSource(Edge edge) {
        return edge.getSource();
    }

    /**
     * Returns the target vertex of an edge. For an undirected graph, source and
     * target are distinguishable designations (but without any mathematical
     * meaning)
     *
     * @param edge
     * @return
     */
    public Vertex getEdgeTarget(Edge edge) {
        return edge.getSink();
    }

    /**
     *
     * @param v
     * @return
     */
    public boolean removeVertex(Vertex v) {
        adj.keySet().stream().filter((key) -> (!adj.get(key).isEmpty()
                && adj.get(key).contains(v))).forEachOrdered((key) -> {
            adj.get(key).remove(v);
        });
        c_adj.keySet().stream().filter((key) -> (!c_adj.get(key).isEmpty()
                && c_adj.get(key).contains(v))).forEachOrdered((key) -> {
            c_adj.get(key).remove(v);
        });

        d_adj.keySet().stream().filter((key) -> (!d_adj.get(key).isEmpty()
                && d_adj.get(key).contains(v))).forEachOrdered((key) -> {
            d_adj.get(key).remove(v);
        });

        adj_type_Map.entrySet().forEach((c) -> {
            c.getValue().stream().filter((e) -> (e.getSource() == v || e.getSink() == v)).forEachOrdered((e) -> {
                adj_type_Map.get(c.getKey()).remove(e);
            });
        });

        adj.remove(v);
        c_adj.remove(v);
        d_adj.remove(v);

        return this.vertices.remove(v);

    }

    /**
     * Return Edges of Type EdgeType (C-Edges/D-Edges etc.)
     *
     * @param e
     * @return Set of edges of type C-Edges/D-Edges etc
     */
    public Set<Edge> getEdgesOfType(EdgeType e) {
        Set<Edge> edgesOfTypes = new HashSet<>();
        if (adj_type_Map.containsKey(e)) {
            edgesOfTypes.addAll(adj_type_Map.get(e));
        }
        return edgesOfTypes;
    }

    public Set<Vertex> getCEdgeNeighbours(Vertex u) {
        validateVertex(u);
        return c_adj.containsKey(u) ? new HashSet<>(c_adj.get(u)) : new HashSet<>();
    }
}

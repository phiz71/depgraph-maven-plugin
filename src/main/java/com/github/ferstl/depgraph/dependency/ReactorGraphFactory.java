package com.github.ferstl.depgraph.dependency;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import com.github.ferstl.depgraph.graph.GraphBuilder;

public class ReactorGraphFactory implements GraphFactory {

  private final ProjectDependencyGraph projectDependencyGraph;
  private final GraphBuilder<DependencyNode> graphBuilder;
  private final DependencyNodeIdRenderer nodeIdRenderer;

  public ReactorGraphFactory(ProjectDependencyGraph projectDependencyGraph, GraphBuilder<DependencyNode> graphBuilder, DependencyNodeIdRenderer nodeIdRenderer) {
    this.projectDependencyGraph = projectDependencyGraph;
    this.graphBuilder = graphBuilder;
    this.nodeIdRenderer = nodeIdRenderer;
  }

  @Override
  public String createGraph(MavenProject project) {
    // Start at the end of the reactor
    List<MavenProject> sortedProjects = this.projectDependencyGraph.getSortedProjects();
    Collections.reverse(sortedProjects);

    Set<String> processedProjects = new HashSet<>();
    for (MavenProject parentProject : sortedProjects) {
      DependencyNode parentNode = new DependencyNode(parentProject.getArtifact());

      for (MavenProject downstreamProject : this.projectDependencyGraph.getDownstreamProjects(parentProject, false)) {
        DependencyNode childNode = new DependencyNode(downstreamProject.getArtifact());

        String nodeString = this.nodeIdRenderer.render(childNode);
        if (!processedProjects.contains(nodeString)) {
          this.graphBuilder.addEdge(parentNode, childNode);
        }
        processedProjects.add(nodeString);
      }

    }

    return this.graphBuilder.toString();
  }
}

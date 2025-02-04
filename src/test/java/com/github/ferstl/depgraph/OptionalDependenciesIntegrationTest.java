/*
 * Copyright (c) 2014 - 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ferstl.depgraph;

import java.io.File;
import java.nio.file.FileSystems;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import static com.github.ferstl.depgraph.MavenVersion.MAX_VERSION;
import static com.github.ferstl.depgraph.MavenVersion.MIN_VERSION;
import static io.takari.maven.testing.TestResources.assertFileContents;
import static io.takari.maven.testing.TestResources.assertFilesPresent;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({MAX_VERSION, MIN_VERSION})
public class OptionalDependenciesIntegrationTest {

  @Rule
  public final TestResources resources = new TestResources();

  private final MavenRuntime mavenRuntime;

  public OptionalDependenciesIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
    this.mavenRuntime = builder
        .withCliOptions("-B")
        .build();
  }

  @Before
  public void before() {
    // Workaround for https://github.com/takari/takari-plugin-testing-project/issues/14
    FileSystems.getDefault();
  }

  @Test
  public void graph() throws Exception {
    File basedir = this.resources.getBasedir("optional-dependencies");
    MavenExecutionResult result = this.mavenRuntime
        .forProject(basedir)
        .execute("clean", "depgraph:graph");

    result.assertErrorFreeLog();
    assertFilesPresent(
        basedir,
        "module-a/target/dependency-graph.dot",
        "module-b/target/dependency-graph.dot",
        "module-c/target/dependency-graph.dot",
        "module-d/target/dependency-graph.dot",
        "module-test/target/dependency-graph.dot",
        "target/dependency-graph.dot");

    String mavenVersion = this.mavenRuntime.getMavenVersion();

    // Maven versions 3.3.x and smaller do not handle the optional dependencies as seen from module-d correctly.
    if (mavenVersion.startsWith(MIN_VERSION)) {
      assertFileContents(basedir, "expectations/graph_module-d-maven33.dot", "module-d/target/dependency-graph.dot");
    } else {
      assertFileContents(basedir, "expectations/graph_module-d.dot", "module-d/target/dependency-graph.dot");
    }
    assertFileContents(basedir, "expectations/graph_module-test.dot", "module-test/target/dependency-graph.dot");
  }

  @Test
  public void aggregate() throws Exception {
    File basedir = this.resources.getBasedir("optional-dependencies");
    MavenExecutionResult result = this.mavenRuntime
        .forProject(basedir)
        .execute("clean", "depgraph:aggregate");

    result.assertErrorFreeLog();

    assertFilesPresent(
        basedir,
        "target/dependency-graph.dot");

    assertFileContents(basedir, "expectations/aggregate_graph.dot", "target/dependency-graph.dot");
  }

  @Test
  public void graphExcludeOptional() throws Exception {
    File basedir = this.resources.getBasedir("optional-dependencies");
    MavenExecutionResult result = this.mavenRuntime
        .forProject(basedir)
        .withCliOption("-DexcludeOptionalDependencies")
        .execute("clean", "depgraph:graph");

    result.assertErrorFreeLog();
    assertFilesPresent(
        basedir,
        "module-a/target/dependency-graph.dot",
        "module-b/target/dependency-graph.dot",
        "module-c/target/dependency-graph.dot",
        "module-d/target/dependency-graph.dot",
        "module-test/target/dependency-graph.dot",
        "target/dependency-graph.dot");

    String mavenVersion = this.mavenRuntime.getMavenVersion();

    // Maven versions 3.3.x and smaller do not handle the optional dependencies as seen from module-d correctly.
    if (mavenVersion.startsWith(MIN_VERSION)) {
      assertFileContents(basedir, "expectations/graphExcludeOptional_module-d-maven33.dot", "module-d/target/dependency-graph.dot");
    } else {
      assertFileContents(basedir, "expectations/graphExcludeOptional_module-d.dot", "module-d/target/dependency-graph.dot");
    }
    assertFileContents(basedir, "expectations/graphExcludeOptional_module-test.dot", "module-test/target/dependency-graph.dot");
  }

  @Test
  public void aggregateExcludeOptional() throws Exception {
    File basedir = this.resources.getBasedir("optional-dependencies");
    MavenExecutionResult result = this.mavenRuntime
        .forProject(basedir)
        .withCliOption("-DexcludeOptionalDependencies")
        .execute("clean", "depgraph:aggregate");

    result.assertErrorFreeLog();

    assertFilesPresent(
        basedir,
        "target/dependency-graph.dot");

    String mavenVersion = this.mavenRuntime.getMavenVersion();

    // Maven versions 3.3.x and smaller do not handle the optional dependencies as seen from module-d correctly.
    if (mavenVersion.startsWith(MIN_VERSION)) {
      assertFileContents(basedir, "expectations/aggregateExcludeOptional_graph-maven33.dot", "target/dependency-graph.dot");
    } else {
      assertFileContents(basedir, "expectations/aggregateExcludeOptional_graph.dot", "target/dependency-graph.dot");
    }
  }
}

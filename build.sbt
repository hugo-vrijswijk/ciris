lazy val ciris = project
  .in(file("."))
  .settings(moduleName := "ciris", name := "Ciris")
  .settings(scalaSettings)
  .settings(noPublishSettings)
  .settings(releaseSettings)
  .settings(testSettings)
  .settings(
    console := (console in (coreJVM, Compile)).value,
    console in Test := (console in (coreJVM, Test)).value
  )
  .aggregate(
    coreJS, coreJVM,
    enumeratumJS, enumeratumJVM,
    genericJS, genericJVM,
    refinedJS, refinedJVM,
    squantsJS, squantsJVM
  )

lazy val core = crossProject
  .in(file("modules/core"))
  .settings(moduleName := "ciris-core", name := "Ciris core")
  .settings(scalaSettings)
  .settings(releaseSettings)
  .settings(testSettings)

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val enumeratum = crossProject
  .in(file("modules/enumeratum"))
  .settings(moduleName := "ciris-enumeratum", name := "Ciris enumeratum")
  .settings(libraryDependencies += "com.beachape" %%% "enumeratum" % "1.5.12")
  .settings(scalaSettings)
  .settings(releaseSettings)
  .settings(testSettings)
  .dependsOn(core % "compile;test->test")

lazy val enumeratumJS = enumeratum.js
lazy val enumeratumJVM = enumeratum.jvm

lazy val generic = crossProject
  .in(file("modules/generic"))
  .settings(moduleName := "ciris-generic", name := "Ciris generic")
  .settings(
    libraryDependencies ++=
      Seq(
        "com.chuusai" %%% "shapeless" % "2.3.2",
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" % Test cross CrossVersion.patch)
      )
  )
  .settings(scalaSettings)
  .settings(releaseSettings)
  .settings(testSettings)
  .dependsOn(core % "compile;test->test")

lazy val genericJS = generic.js
lazy val genericJVM = generic.jvm

lazy val refined = crossProject
  .in(file("modules/refined"))
  .settings(moduleName := "ciris-refined", name := "Ciris refined")
  .settings(libraryDependencies += "eu.timepit" %%% "refined" % "0.8.1")
  .settings(scalaSettings)
  .settings(releaseSettings)
  .settings(testSettings)
  .dependsOn(core % "compile;test->test")

lazy val refinedJS = refined.js
lazy val refinedJVM = refined.jvm

lazy val squants = crossProject
  .in(file("modules/squants"))
  .settings(moduleName := "ciris-squants", name := "Ciris squants")
  .settings(libraryDependencies += "org.typelevel" %%% "squants" % "1.2.0")
  .settings(scalaSettings)
  .settings(releaseSettings)
  .settings(testSettings)
  .dependsOn(core % "compile;test->test")

lazy val squantsJS = squants.js
lazy val squantsJVM = squants.jvm

import com.typesafe.sbt.SbtGit.GitKeys._
lazy val docs = project
  .in(file("docs"))
  .settings(moduleName := "ciris-docs", name := "Ciris docs")
  .settings(scalaSettings)
  .settings(noPublishSettings)
  .settings(
    micrositeName := "Ciris",
    micrositeDescription := "Lightweight, extensible, and validated configuration loading in Scala",
    micrositeDocumentationUrl := "api",
    micrositeAuthor := "Viktor Lövgren",
    micrositeOrganizationHomepage := "https://vlovgr.se",
    micrositeAnalyticsToken := "UA-37804684-4",
    micrositeGithubOwner := "vlovgr",
    micrositeGithubRepo := "ciris",
    micrositeTwitterCreator := "@vlovgr",
    micrositeHighlightTheme := "atom-one-light",
    micrositePalette := Map(
      "brand-primary" -> "#3e4959",
      "brand-secondary" -> "#3e4959",
      "brand-tertiary" -> "#3e4959",
      "gray-dark" -> "#3e4959",
      "gray" -> "#837f84",
      "gray-light" -> "#e3e2e3",
      "gray-lighter" -> "#f4f3f4",
      "white-color" -> "#f3f3f3"
    )
  )
  .settings(
    buildInfoObject := "info",
    buildInfoPackage := "ciris.build",
    buildInfoKeys := Seq[BuildInfoKey](
      organization,
      latestVersion in ThisBuild,
      crossScalaVersions,
      BuildInfoKey.map(moduleName in coreJVM) { case (k, v) => "core" + k.capitalize -> v },
      BuildInfoKey.map(moduleName in enumeratumJVM) { case (k, v) => "enumeratum" + k.capitalize -> v },
      BuildInfoKey.map(moduleName in genericJVM) { case (k, v) => "generic" + k.capitalize -> v },
      BuildInfoKey.map(moduleName in refinedJVM) { case (k, v) => "refined" + k.capitalize -> v },
      BuildInfoKey.map(moduleName in squantsJVM) { case (k, v) => "squants" + k.capitalize -> v }
    ),
    scalaVersion := "2.12.1" // sbt-buildinfo 0.7.0 broken on 2.12.2
  )
  .settings(
    generateApiIndexFile := {
      val target = resourceManaged.value / "api.txt"
      val version = (latestVersion in ThisBuild).value
      val scalaTargetVersion = scalaVersion.value.split('.').init.mkString(".")

      val content =
        s"""
          |This is the API documentation for [[https://cir.is Ciris]]: lightweight, extensible, and validated configuration loading in Scala.
          |
          |The documentation is kept up-to-date with new releases, currently documenting release [[https://github.com/vlovgr/ciris/releases/tag/v$version v$version]] on Scala $scalaTargetVersion.
          |
          |Ciris is divided into the following set of modules.
          |
          | - The [[ciris core]] module provides basic functionality and support for reading standard library types.
          | - The [[ciris.enumeratum enumeratum]] module integrates with [[https://github.com/lloydmeta/enumeratum enumeratum]] to be able to read enumerations.
          | - The [[ciris.generic generic]] module uses [[https://github.com/milessabin/shapeless shapeless]] to be able to read unary products, and coproducts.
          | - The [[ciris.refined refined]] module integrates with [[https://github.com/fthomas/refined refined]] to be able to read refinement types.
          | - The [[ciris.squants squants]] module integrates with [[http://www.squants.com squants]] to read values with unit of measure.
          |
          |If you're looking for an overview, with examples and explanations of the most common use cases, please refer to the [[https://cir.is/docs/basics usage guide]].
        """.stripMargin.trim

      IO.write(target, content)
      target
    },
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(noDocumentationProjects: _*),
    siteSubdirName in ScalaUnidoc := micrositeDocumentationUrl.value,
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
    gitRemoteRepo := "git@github.com:vlovgr/ciris.git",
    scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
      "-skip-packages", buildInfoPackage.value,
      "-doc-source-url", s"https://github.com/vlovgr/ciris/tree/v${(latestVersion in ThisBuild).value}€{FILE_PATH}.scala",
      "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
      "-doc-root-content", (generateApiIndexFile.value).getAbsolutePath
    )
  )
  .dependsOn(coreJVM, enumeratumJVM, genericJVM, refinedJVM, squantsJVM)
  .enablePlugins(BuildInfoPlugin, MicrositesPlugin, ScalaUnidocPlugin)

lazy val scala210 = "2.10.6"
lazy val scala211 = "2.11.11"
lazy val scala212 = "2.12.2"

lazy val scalaSettings = Seq(
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala210, scala211, scala212),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import",
    "-Ywarn-unused"
  ).filter {
    case "-Ywarn-unused-import" if (scalaVersion.value startsWith "2.10") => false
    case "-Ywarn-unused" if !(scalaVersion.value startsWith "2.12") => false
    case _ => true
  },
  scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val metadataSettings = Seq(
  name := "Ciris",
  organization := "is.cir",
  organizationName := "Ciris",
  organizationHomepage := Some(url("https://cir.is"))
)

import ReleaseTransformations._
lazy val releaseSettings =
  metadataSettings ++ Seq(
    homepage := organizationHomepage.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    useGpg := true,
    pomIncludeRepository := { _ => false },
    autoAPIMappings := true,
    apiURL := Some(url("https://cir.is/api")),
    licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/vlovgr/ciris"),
        "scm:git@github.com:vlovgr/ciris.git"
      )
    ),
    developers := List(
      Developer(
        id = "vlovgr",
        name = "Viktor Lövgren",
        email = "github@vlovgr.se",
        url = url("https://vlovgr.se")
      )
    ),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if(isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    releaseCrossBuild := true,
    releaseTagName := s"v${(version in ThisBuild).value}",
    releaseTagComment := s"Release version ${(version in ThisBuild).value}",
    releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}",
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseUseGlobalVersion := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      setLatestVersion,
      releaseStepTask(updateReadme in ThisBuild),
      releaseStepTask(updateScripts in ThisBuild),
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      releaseStepCommand("sonatypeRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges,
      releaseStepCommand("project docs"),
      releaseStepCommand("publishMicrosite")
    )
  )

lazy val testSettings = Seq(
  logBuffered in Test := false,
  parallelExecution in Test := false,
  testOptions in Test += Tests.Argument("-oDF"),
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.0.3" % Test,
    "org.scalacheck" %%% "scalacheck" % "1.13.5" % Test
  )
)

lazy val noPublishSettings =
  metadataSettings ++ Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )

lazy val sourceGeneratorSettings = Seq(
  sourceGenerators in Compile +=
    Def.task(generateSources(
      (sourceManaged in Compile).value,
      (sourceManaged in Test).value,
      "ciris"
    )).taskValue
)

val generateReadme = taskKey[File]("Generates the readme")
generateReadme in ThisBuild := {
  (tut in docs).value
  val source = IO.read((tutTargetDirectory in docs).value / "index.md")
  val readme = source.replaceAll("^\\s*---[^(---)]*---\\s*", "")
  val target = (baseDirectory in ciris).value / "readme.md"
  IO.write(target, readme)
  target
}

val updateReadme = taskKey[Unit]("Generates and commits the readme")
updateReadme in ThisBuild := {
  (generateReadme in ThisBuild).value
  sbtrelease.Vcs.detect((baseDirectory in ciris).value).foreach { vcs =>
    vcs.add("readme.md").!
    vcs.commit("Update readme to latest version", sign = true).!
  }
}

val scriptsDirectory = "scripts"

val generateScripts = taskKey[Unit]("Generates scripts")
generateScripts in ThisBuild := {
  val output = file(scriptsDirectory)
  val organizationId = (organization in coreJVM).value
  val moduleVersion = (latestVersion in ThisBuild).value

  val tryScript =
    s"""
       |#!/usr/bin/env sh
       |test -e ~/.coursier/coursier || ( \\
       |  mkdir -p ~/.coursier && \\
       |  curl -Lso ~/.coursier/coursier https://git.io/vgvpD && \\
       |  chmod +x ~/.coursier/coursier \\
       |)
       |
       |~/.coursier/coursier launch -q -P \\
       |  com.lihaoyi:ammonite_2.12.2:0.9.3 \\
       |  $organizationId:${(moduleName in coreJVM).value}_2.12:$moduleVersion \\
       |  $organizationId:${(moduleName in enumeratumJVM).value}_2.12:$moduleVersion \\
       |  $organizationId:${(moduleName in genericJVM).value}_2.12:$moduleVersion \\
       |  $organizationId:${(moduleName in refinedJVM).value}_2.12:$moduleVersion \\
       |  $organizationId:${(moduleName in squantsJVM).value}_2.12:$moduleVersion \\
       |  -- --predef 'import ciris._,ciris.enumeratum._,ciris.generic._,ciris.refined._,ciris.squants._' < /dev/tty
     """.stripMargin.trim + "\n"

  IO.createDirectory(output)
  IO.write(output / "try.sh", tryScript)
}

val updateScripts = taskKey[Unit]("Generates and commits scripts")
updateScripts in ThisBuild := {
  (generateScripts in ThisBuild).value
  sbtrelease.Vcs.detect((baseDirectory in ciris).value).foreach { vcs =>
    vcs.add(scriptsDirectory).!
    vcs.commit("Update scripts to latest version", sign = true).!
  }
}

val generateApiIndexFile = taskKey[File]("Generates the API index file")

lazy val crossModules: Seq[(Project, Project)] =
  Seq(
    (coreJVM, coreJS),
    (enumeratumJVM, enumeratumJS),
    (genericJVM, genericJS),
    (refinedJVM, refinedJS),
    (squantsJVM, squantsJS)
  )

lazy val noDocumentationProjects: Seq[ProjectReference] =
  crossModules.map { case(_, js) => (js: ProjectReference) }

lazy val allModules = List("core", "enumeratum", "generic", "refined", "squants")
lazy val allModulesJS = allModules.map(_ + "JS")
lazy val allModulesJVM = allModules.map(_ + "JVM")

def addCommandsAlias(name: String, values: List[String]) =
  addCommandAlias(name, values.mkString(";", ";", ""))

addCommandsAlias("testJS", allModulesJS.map(_ + "/test"))

addCommandsAlias("testJVM", allModulesJVM.map(_ + "/test"))

addCommandsAlias("validate", List(
  "clean",
  "testJS",
  "coverage",
  "testJVM",
  "coverageReport",
  "coverageOff"
))

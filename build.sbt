ThisBuild / organization := "io.simplifier"
ThisBuild / version := sys.env.get("VERSION").getOrElse("NA")
ThisBuild / scalaVersion := "2.12.15"

ThisBuild / useCoursier := true

lazy val keyValueStorePlugin = (project in file("."))
  .settings(
    name := "KeyValueStorePlugin",
    assembly / assemblyJarName := "keyValueStorePlugin.jar",
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    libraryDependencies ++= Seq(
      "com.mysql"                % "mysql-connector-j"       % "8.2.0"      exclude("com.google.protobuf", "protobuf-java"),
      "org.mapdb"                % "mapdb"                   % "2.0-beta11" withSources() withJavadoc(),
      "io.github.simplifier-ag" %% "simplifier-plugin-base"  % "1.0.0"      withSources()
    )
  )

//Security Options for Java >= 18
val moduleSecurityRuntimeOptions = Seq(
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/sun.security.jca=ALL-UNNAMED",
  // used by W3CXmlUtil
  "--add-exports=java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED"
)

run / javaOptions ++= moduleSecurityRuntimeOptions
run / fork := true
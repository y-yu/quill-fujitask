name := "quill-fujitask"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.4.192",
  "io.getquill" %% "quill-jdbc" % "1.1.0",
  "org.scalikejdbc" %% "scalikejdbc" % "2.5.1",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.5.1"
)
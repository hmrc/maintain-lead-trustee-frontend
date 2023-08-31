resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.9.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.2.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")

addSbtPlugin("com.beautiful-scala" % "sbt-scalastyle" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")

addSbtPlugin("io.github.irundaia" % "sbt-sassify" % "1.5.2")

addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "0.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "2.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
